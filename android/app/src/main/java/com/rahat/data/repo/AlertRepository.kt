package com.rahat.data.repo

import com.rahat.data.model.Alert
import com.rahat.data.model.AlertSeverity
import kotlin.random.Random

/**
 * Senior Architect Implementation: AlertRepository
 * 
 * MANDATORY FIX: Removed all hardcoded demo/fake alerts.
 * Data must now flow only from the BLE mesh and real-time backend updates.
 */
class AlertRepository {

    private val _alerts = mutableListOf<Alert>()
    
    fun getAlerts(): List<Alert> {
        return _alerts.toList()
    }

    /**
     * CLEANUP: fake disaster seeds were removed as per architectural guidelines.
     */
    fun generateDemoSeeds(centerLat: Double, centerLon: Double) {
        // No-op. Demo alerts are forbidden.
    }

    private fun createSeed(lat: Double, lon: Double, severity: AlertSeverity, msg: String): Alert {
        return Alert(
            id = java.util.UUID.randomUUID().toString(),
            lat = lat,
            lon = lon,
            severity = severity,
            message = msg,
            timestamp = System.currentTimeMillis()
        )
    }
}
