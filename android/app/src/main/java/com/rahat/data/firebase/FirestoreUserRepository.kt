package com.rahat.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class FirestoreUser(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val rId: String = "", // Added unique Rahat ID
    val role: List<String> = listOf("user"),
    val lastKnownLocation: Map<String, Any>? = null,
    val createdAt: com.google.firebase.Timestamp? = null
)

class FirestoreUserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    
    suspend fun createUser(
        rId: String,
        name: String,
        phone: String,
        email: String = ""
    ) {
        val user = hashMapOf(
            "name" to name,
            "phone" to phone,
            "email" to email,
            "rId" to rId,
            "role" to listOf("user"),
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        
        usersCollection.document(rId).set(user).await()
    }

    private fun generateRId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val randomStr = (1..4)
            .map { chars.random() }
            .joinToString("")
        return "RAHAT_$randomStr"
    }
    
    suspend fun getUser(uid: String): FirestoreUser? {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.toObject(FirestoreUser::class.java)
    }
    
    suspend fun updateUserLocation(
        uid: String,
        lat: Double,
        lng: Double
    ) {
        val location = hashMapOf(
            "lastKnownLocation" to hashMapOf(
                "lat" to lat,
                "lng" to lng,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
        )
        
        usersCollection.document(uid).set(location, SetOptions.merge()).await()
    }
    
    suspend fun updateUserName(uid: String, name: String) {
        usersCollection.document(uid)
            .update("name", name)
            .await()
    }
}
