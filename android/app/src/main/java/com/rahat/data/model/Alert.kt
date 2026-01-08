package com.rahat.data.model

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class Alert(
    val id: String,
    val lat: Double,
    val lon: Double,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long
)
