package com.rahat.data.model

enum class SignalLevel {
    VERY_STRONG, STRONG, MODERATE, WEAK
}

enum class SignalTrend {
    APPROACHING, RECEDING, STABLE
}

data class PeerState(
    val rId: String,
    val name: String,
    val severity: String = "HIGH",
    val signalLevel: SignalLevel = SignalLevel.WEAK,
    val signalTrend: SignalTrend = SignalTrend.STABLE,
    val lastSeen: Long = System.currentTimeMillis(),
    val source: PeerSource = PeerSource.DIRECT,
    val latitude: Double? = null,
    val longitude: Double? = null
)

enum class PeerSource {
    DIRECT, MESH
}
