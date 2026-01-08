package com.rahat.service.nearby

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.rahat.data.model.NearbyDevice
import com.rahat.data.repo.MeshRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.take
import java.util.*

class NearbyManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private val advertiser: BluetoothLeAdvertiser? by lazy { bluetoothAdapter?.bluetoothLeAdvertiser }
    private val scanner: BluetoothLeScanner? by lazy { bluetoothAdapter?.bluetoothLeScanner }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            processScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            results.forEach { processScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("NearbyManager", "Scan failed: $errorCode")
        }
    }

    // RAHAT Service UUID
    private val SERVICE_UUID = ParcelUuid.fromString("0000aaaa-0000-1000-8000-00805f9b34fb")
    
    private var advertisingJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentAdvCallback: AdvertiseCallback? = null

    @SuppressLint("MissingPermission")
    fun startAdvertisingLoop(rId: String, name: String, lat: Double, lng: Double, severity: String) {
        if (advertisingJob?.isActive == true) return
        
        advertisingJob = serviceScope.launch {
            while (isActive) {
                // 1. Broadcast SELF (6 seconds)
                val selfPayload = "$rId|$severity|${lat.toString().take(7)}|${lng.toString().take(7)}"
                broadcastPayload(selfPayload)
                delay(6000)

                // 2. Broadcast High Priority Neighbor (Relay) (4 seconds)
                // Find a Critical/High neighbor that isn't me
                val neighbors = MeshRepository.nearbyPeers.value
                val urgentNeighbor = neighbors.firstOrNull { 
                    (it.severity == "CRITICAL" || it.severity == "HIGH") && it.rId != rId 
                }

                if (urgentNeighbor != null) {
                    val relayPayload = "${urgentNeighbor.rId}|${urgentNeighbor.severity}|${urgentNeighbor.latitude.toString().take(7)}|${urgentNeighbor.longitude.toString().take(7)}"
                    broadcastPayload(relayPayload)
                    delay(4000)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun broadcastPayload(payload: String) {
        if (advertiser == null) return
        
        // Stop previous to avoid error 1 (Data too large or multiple ads)
        stopCurrentAdvertisement()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceData(SERVICE_UUID, payload.toByteArray(Charsets.UTF_8))
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d("NearbyManager", "Broadcasting: $payload")
            }
            override fun onStartFailure(errorCode: Int) {
                 Log.e("NearbyManager", "Broadcast failed: $errorCode")
            }
        }
        
        currentAdvCallback = callback
        advertiser?.startAdvertising(settings, data, callback)
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        advertisingJob?.cancel()
        stopCurrentAdvertisement()
    }
    
    @SuppressLint("MissingPermission")
    private fun stopCurrentAdvertisement() {
        if (currentAdvCallback != null && advertiser != null) {
            try {
                 advertiser?.stopAdvertising(currentAdvCallback)
            } catch (e: Exception) {
                // Ignore
            }
            currentAdvCallback = null
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (scanner == null) return
        
        val filter = ScanFilter.Builder()
            .setServiceUuid(SERVICE_UUID)
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(listOf(filter), settings, scanCallback)
        Log.d("NearbyManager", "Scanning started")
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        scanner?.stopScan(scanCallback)
    }

    private fun processScanResult(result: ScanResult) {
        val bytes = result.scanRecord?.getServiceData(SERVICE_UUID) ?: return
        val payload = String(bytes, Charsets.UTF_8)
        
        try {
            val parts = payload.split("|")
            if (parts.size >= 4) {
                val rId = parts[0]
                val severity = parts[1]
                val lat = parts[2].toDoubleOrNull() ?: 0.0
                val lng = parts[3].toDoubleOrNull() ?: 0.0
                
                val device = com.rahat.data.model.PeerState(
                    rId = rId,
                    name = "User ($rId)",
                    latitude = lat,
                    longitude = lng,
                    severity = severity,
                    lastSeen = System.currentTimeMillis()
                )
                
                MeshRepository.updatePeer(device)
            }
        } catch (e: Exception) {
            Log.e("NearbyManager", "Error parsing payload: ${e.message}")
        }
    }
}
