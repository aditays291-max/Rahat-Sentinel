package com.rahat.service.ble

class KalmanFilter(
    private var q: Double = 0.1,  // Process noise covariance
    private var r: Double = 0.1,  // Measurement noise covariance
    private var x: Double = -70.0, // Last estimate
    private var p: Double = 1.0   // Estimation error covariance
) {
    fun update(measurement: Double): Double {
        // Prediction update
        p += q
        
        // Measurement update
        val k = p / (p + r) // Kalman gain
        x += k * (measurement - x)
        p *= (1 - k)
        
        return x
    }
}
