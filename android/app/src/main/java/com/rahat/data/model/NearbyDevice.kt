package com.rahat.data.model

data class NearbyDevice(
    val rId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val severity: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val timestamp: Long = System.currentTimeMillis()
) {
    // Helper to calculate distance from current user
    fun getDistanceInMeters(userLat: Double, userLng: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(latitude, longitude, userLat, userLng, results)
        return results[0]
    }
}
