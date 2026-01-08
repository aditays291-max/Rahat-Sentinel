package com.rahat.service.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer

class BleBeaconManager(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val advertiser: BluetoothLeAdvertiser? by lazy { bluetoothAdapter?.bluetoothLeAdvertiser }
    private val scanner: BluetoothLeScanner? by lazy { bluetoothAdapter?.bluetoothLeScanner }
    
    // Custom UUID for Rahat Beacons - using a 16-bit UUID (0xAAAA) to save space.
    // 31 bytes limit: Flags(3) + 16-bit Service Data [Length(1) + Type(1) + UUID(2) + Payload(17)] = 24 bytes. Fits!
    private val SERVICE_UUID = ParcelUuid.fromString("0000AAAA-0000-1000-8000-00805f9b34fb")

    @SuppressLint("MissingPermission")
    fun startAdvertising(ephemeralId: String, lat: Double, lng: Double, battery: Int) {
        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false) // Beacon only
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build()
                
            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceData(SERVICE_UUID, buildPayload(ephemeralId, lat, lng, battery))
                .build()
                
            advertiser?.startAdvertising(settings, data, advCallback)
            Log.d("BleBeacon", "startAdvertising called with ephId: $ephemeralId")
        } catch (e: Exception) {
            Log.e("BleBeacon", "Advertising failed to start: ${e.message}")
        }
    }
    
    private val advCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
             Log.d("BleBeacon", "Advertising Started Successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            val msg = when(errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "DATA_TOO_LARGE"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "TOO_MANY_ADVERTISERS"
                ADVERTISE_FAILED_ALREADY_STARTED -> "ALREADY_STARTED"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "INTERNAL_ERROR"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "FEATURE_UNSUPPORTED"
                else -> "UNKNOWN_$errorCode"
            }
            Log.e("BleBeacon", "Advertising Failed: $msg")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        try {
            advertiser?.stopAdvertising(advCallback)
        } catch (e: Exception) {
            Log.e("BleBeacon", "Stop advertising failed", e)
        }
    }

    private fun buildPayload(ephId: String, lat: Double, lng: Double, bat: Int): ByteArray {
        // Simple packing: ID(8) + Lat(4) + Lng(4) + Bat(1) = 17 bytes
        val buffer = ByteBuffer.allocate(17)
        buffer.put(ephId.chunked(2).map { it.toInt(16).toByte() }.toByteArray()) // 8 bytes (assuming hex input)
        buffer.putFloat(lat.toFloat())
        buffer.putFloat(lng.toFloat())
        buffer.put(bat.toByte())
        return buffer.array()
    }
    
    // Scanner Logic
    private var scanCallback: ScanCallback? = null

    @SuppressLint("MissingPermission")
    fun startScanning(onPeerFound: (String, Double, Double, Int, Int) -> Unit) {
        val filter = ScanFilter.Builder()
            .setServiceUuid(SERVICE_UUID)
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
            
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val data = result.scanRecord?.getServiceData(SERVICE_UUID) ?: return
                if (data.size >= 17) {
                    try {
                        val buffer = ByteBuffer.wrap(data)
                        val idBytes = ByteArray(8)
                        buffer.get(idBytes)
                        val ephId = idBytes.joinToString("") { "%02x".format(it) }
                        
                        val lat = buffer.getFloat().toDouble()
                        val lng = buffer.getFloat().toDouble()
                        val bat = buffer.get().toInt()
                        
                        onPeerFound(ephId, lat, lng, bat, result.rssi)
                    } catch (e: Exception) {
                        Log.e("BleBeacon", "Parse error", e)
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BleBeacon", "Scan failed with error: $errorCode")
            }
        }
        
        try {
            scanner?.startScan(listOf(filter), settings, scanCallback)
        } catch (e: Exception) {
            Log.e("BleBeacon", "Scan failed to start: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        scanCallback?.let { scanner?.stopScan(it) }
    }
}
