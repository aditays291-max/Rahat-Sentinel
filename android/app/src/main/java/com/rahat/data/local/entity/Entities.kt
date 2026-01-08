package com.rahat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_table")
data class DeviceEntity(
    @PrimaryKey val rId: String,
    val createdAt: Long
)

@Entity(tableName = "ephemeral_id_table")
data class EphemeralIdEntity(
    @PrimaryKey val ephemeralId: String,
    val generatedAt: Long,
    val expiresAt: Long
)

@Entity(tableName = "seen_peers_table")
data class SeenPeerEntity(
    @PrimaryKey val ephemeralId: String,
    val avgRssi: Int,
    val lastSeen: Long,
    val coarseLat: Double,
    val coarseLon: Double
)

@Entity(tableName = "user_profile_table")
data class UserProfileEntity(
    @PrimaryKey val rId: String,
    val phone: String,
    val name: String = "Rahat User",
    val settingsJson: String
)

@Entity(tableName = "emergency_contact_table")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String,
    val name: String,
    val relation: String,
    val phone: String
)
