package com.rahat.service.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.util.Log
import java.nio.ByteBuffer

/**
 * Senior Architect Implementation: BleAdvertiser
 * 
 * DESIGN PRINCIPLES:
 * 1. PERSISTENCE: Start once, stay active.
 * 2. SEAMLESS UPDATES: Use AdvertisingSet (API 26+) for zero-downtime payload rotation.
 * 3. RANGE MAXIMIZATION: Use LE Coded PHY if supported for long-distance mesh.
 * 4. POWER: TX_POWER_HIGH for maximum penetration.
 */
class BleAdvertiser(private val context: Context) {

    private val TAG = "BLE_ADVERTISER"
    private val MANUFACTURER_ID = 0xFFFF

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val advertiser: BluetoothLeAdvertiser? by lazy { bluetoothAdapter?.bluetoothLeAdvertiser }

    private var currentAdvertisingSet: AdvertisingSet? = null
    private var isAdvertising = false
    private var lastPayload: ByteArray? = null
    private var startTime = 0L

    private val callback = object : AdvertisingSetCallback() {
        override fun onAdvertisingSetStarted(advertisingSet: AdvertisingSet, txPower: Int, status: Int) {
            if (status == ADVERTISE_SUCCESS) {
                startTime = System.currentTimeMillis()
                Log.i(TAG, "BLE_ADVERTISER_STARTED_SUCCESS: (TX Power: $txPower)")
                currentAdvertisingSet = advertisingSet
                isAdvertising = true
            } else {
                Log.e(TAG, "BLE_ADVERTISER_START_FAILED: Status $status")
                isAdvertising = false
            }
        }

        override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet) {
            val uptime = (System.currentTimeMillis() - startTime) / 1000
            Log.w(TAG, "BLE_ADVERTISER_STOPPED: Hardware set stopped. Uptime: ${uptime}s")
            isAdvertising = false
            currentAdvertisingSet = null
        }

        override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet, status: Int) {
            if (status == ADVERTISE_SUCCESS) {
                Log.d(TAG, "BLE_ADVERTISER_PAYLOAD_UPDATED_SEAMLESSLY")
            } else {
                Log.e(TAG, "BLE_ADVERTISER_PAYLOAD_UPDATE_FAILED: Status $status")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startOrUpdateAdvertising(ephemeralId: String, severity: Int, isMoving: Boolean) {
        val adapterLocal = bluetoothAdapter ?: return
        val advertiserLocal = advertiser ?: run {
            Log.e(TAG, "BLE_ADVERTISER_ABORTED: No BLE Hardware")
            return
        }
        
        val payload = buildManufacturerPayload(ephemeralId, severity, isMoving)

        // Rule: Do NOT update if same (Ignore isMoving updates as per new contract)
        if (lastPayload?.contentEquals(payload) == true && isAdvertising) {
            return
        }
        lastPayload = payload

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addManufacturerData(MANUFACTURER_ID, payload)
            .build()

        val currentSet = currentAdvertisingSet
        if (currentSet != null && isAdvertising) {
            Log.d(TAG, "BLE_ADVERTISER_ROTATING_PAYLOAD: EphID: $ephemeralId")
            currentSet.setAdvertisingData(data)
        } else {
            Log.i(TAG, "BLE_ADVERTISER_INITIAL_START: EphID: $ephemeralId")
            
            // SENIOR ARCHITECT DECISION: Force Legacy Mode (LE_1M) for universal discovery.
            val parameters = AdvertisingSetParameters.Builder()
                .setInterval(AdvertisingSetParameters.INTERVAL_LOW)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
                .setConnectable(false)
                .setLegacyMode(true) 
                .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
                .setSecondaryPhy(BluetoothDevice.PHY_LE_1M)
                .build()

            Log.i(TAG, "BLE_PHY_ENFORCED: Legacy LE_1M (Maximum Compatibility)")
            advertiserLocal.startAdvertisingSet(parameters, data, null, null, null, callback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (!isAdvertising) return
        try {
            val uptime = (System.currentTimeMillis() - startTime) / 1000
            Log.w(TAG, "BLE_ADVERTISER_STOP_REQUESTED: Terminating session. Total Uptime: ${uptime}s")
            advertiser?.stopAdvertisingSet(callback)
        } catch (e: Exception) {
            Log.e(TAG, "BLE_ADVERTISER_STOP_ERR: ${e.message}")
        }
    }

    private fun buildManufacturerPayload(ephId: String, severity: Int, isMoving: Boolean): ByteArray {
        val buffer = ByteBuffer.allocate(17) // 16 bytes EphID + 1 byte Status
        try {
            // Hex parsing: 32 hex chars = 16 bytes
            val idBytes = ByteArray(16)
            for (i in 0 until 16) {
                idBytes[i] = ephId.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
            buffer.put(idBytes)
            
            // Coarse status flag: 1 = SOS, 0 = Normal
            val statusFlag = if (severity >= 2) 1.toByte() else 0.toByte()
            buffer.put(statusFlag)
            
            Log.d(TAG, "BLE_ADVERTISER_PAYLOAD_BUILT: ID=${ephId.take(8)}... Status=${if (statusFlag == 1.toByte()) "SOS" else "Normal"}")
        } catch (e: Exception) {
            Log.e(TAG, "BLE_ADVERTISER_PAYLOAD_ERR: Invalid EphID format? $ephId")
        }
        return buffer.array()
    }
}
