package com.rahat.service.ble

import android.util.Log
import com.rahat.data.model.PeerSource
import com.rahat.data.model.PeerState
import com.rahat.data.model.SignalLevel
import com.rahat.data.model.SignalTrend
import com.rahat.data.repo.MeshRepository
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.abs

/**
 * Senior Architect Implementation: PeerManager
 * 
 * DESIGN PRINCIPLES:
 * 1. HIGH FIDELITY: Buffered RSSI (10 samples) for stability.
 * 2. TREND ANALYSIS: Slope-based approaching/receding detection.
 * 3. NO GUESSWORK: Removed misleading meter-based distance estimation.
 * 4. UI STABILITY: 3s update throttle.
 */
class PeerManager(private val scope: CoroutineScope) {
    fun getScope(): CoroutineScope = scope

    private val TAG = "RAHAT_PEER_MANAGER"
    private val TTL_MS = 60_000L // 60s as requested
    private val UI_UPDATE_INTERVAL_MS = 3_000L
    private val SIGNAL_WINDOW_SIZE = 10

    // Internal Peer State
    private class PeerData(
        val macAddress: String,
        var currentEphId: String,
        var severity: Int
    ) {
        var lastSeen = System.currentTimeMillis()
        val rssiHistory = mutableListOf<Int>()
        var lastUiUpdateTime = 0L
        var currentTrend = SignalTrend.STABLE

        fun addRssi(rssi: Int) {
            rssiHistory.add(rssi)
            if (rssiHistory.size > 10) rssiHistory.removeAt(0)
            lastSeen = System.currentTimeMillis()
        }

        fun getFilteredRssi(): Double {
            if (rssiHistory.isEmpty()) return -100.0
            val sorted = rssiHistory.sorted()
            return sorted[sorted.size / 2].toDouble() // Median
        }

        /**
         * Computes slope of RSSI trend.
         * Slope > 0.5: Approaching (Signal improving)
         * Slope < -0.5: Receding (Signal fading)
         */
        fun computeTrend(): SignalTrend {
            if (rssiHistory.size < 6) return SignalTrend.STABLE
            
            val half = rssiHistory.size / 2
            val firstHalfAvg = rssiHistory.take(half).average()
            val secondHalfAvg = rssiHistory.takeLast(half).average()
            
            val rawSlope = (secondHalfAvg - firstHalfAvg) / half
            
            val newTrend = when {
                rawSlope > 0.6 -> SignalTrend.APPROACHING
                rawSlope < -0.6 -> SignalTrend.RECEDING
                else -> SignalTrend.STABLE
            }

            if (newTrend != currentTrend) {
                Log.i("RAHAT_PEER_MANAGER", "TREND_CHANGE: Peer $macAddress is now $newTrend (Slope: ${"%.2f".format(rawSlope)})")
                currentTrend = newTrend
            }
            return newTrend
        }

        fun getSignalLevel(filteredRssi: Double): SignalLevel {
            return when {
                filteredRssi > -60.0 -> SignalLevel.VERY_STRONG
                filteredRssi > -75.0 -> SignalLevel.STRONG
                filteredRssi > -90.0 -> SignalLevel.MODERATE
                else -> SignalLevel.WEAK
            }
        }
    }

    private val activePeers = Collections.synchronizedMap(mutableMapOf<String, PeerData>())

    init {
        // TTL Housekeeping
        scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val toRemove = activePeers.filterValues { now - it.lastSeen >= TTL_MS }.keys
                if (toRemove.isNotEmpty()) {
                    Log.i(TAG, "PEER_REMOVED: TTL expired for ${toRemove.size} peers")
                    toRemove.forEach { activePeers.remove(it) }
                    syncWithRepository()
                }
                delay(5000)
            }
        }
    }

    fun onRawPeerDiscovery(
        mac: String,
        ephId: String,
        severity: Int,
        isMoving: Boolean, // Ignored as per new contract
        rawRssi: Int
    ) {
        val now = System.currentTimeMillis()
        
        val peer = activePeers.getOrPut(mac) {
            Log.i(TAG, "PEER_DISCOVERED: $mac")
            PeerData(mac, ephId, severity)
        }

        peer.currentEphId = ephId
        peer.addRssi(rawRssi)

        // UI UPDATE THROTTLING
        if (now - peer.lastUiUpdateTime >= UI_UPDATE_INTERVAL_MS) {
            val filteredRssi = peer.getFilteredRssi()
            val trend = peer.computeTrend()
            val level = peer.getSignalLevel(filteredRssi)
            
            Log.d(TAG, "RAHAT_MESH_SIGNAL_REPORT: MAC=$mac | RawRSS=$rawRssi | FilteredRSS=${filteredRssi.toInt()} | Level=$level | Trend=$trend")
            
            peer.lastUiUpdateTime = now
            syncWithRepository()
        }
    }

    private fun syncWithRepository() {
        val peers = activePeers.values.map { data ->
            val filteredRssi = data.getFilteredRssi()
            PeerState(
                rId = "PEER_${data.currentEphId.take(6)}",
                name = "Nearby Device (${data.macAddress.takeLast(4)})",
                severity = if (data.severity == 2) "HIGH" else "NORMAL",
                signalLevel = data.getSignalLevel(filteredRssi),
                signalTrend = data.computeTrend(),
                lastSeen = data.lastSeen,
                source = PeerSource.DIRECT
            )
        }
        MeshRepository.setPeers(peers)
    }
}
