package com.rahat.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class FirestoreAlert(
    val type: String = "",  // "SOS", "EARTHQUAKE", "FLOOD", "DEMO"
    val severity: String = "",  // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val location: Map<String, Double> = mapOf(),
    val radius: Int = 2000,
    val triggeredBy: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val active: Boolean = true
)

class FirestoreAlertRepository {
    private val db = FirebaseFirestore.getInstance()
    private val alertsCollection = db.collection("alerts")
    
    suspend fun createAlert(
        type: String,
        severity: String,
        lat: Double,
        lng: Double,
        triggeredBy: String,
        radius: Int = 2000
    ): String {
        val alert = hashMapOf(
            "type" to type,
            "severity" to severity,
            "location" to hashMapOf(
                "lat" to lat,
                "lng" to lng
            ),
            "radius" to radius,
            "triggeredBy" to triggeredBy,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "active" to true
        )
        
        val docRef = alertsCollection.add(alert).await()
        return docRef.id
    }
    
    suspend fun getActiveAlerts(): List<FirestoreAlert> {
        val snapshot = alertsCollection
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { 
            it.toObject(FirestoreAlert::class.java)
        }
    }
    
    suspend fun deactivateAlert(alertId: String) {
        alertsCollection.document(alertId)
            .update("active", false)
            .await()
    }
}
