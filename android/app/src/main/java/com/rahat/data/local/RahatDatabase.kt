package com.rahat.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.rahat.data.local.entity.*

@Dao
interface RahatDao {
    // Device
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Query("SELECT * FROM device_table LIMIT 1")
    fun getDevice(): Flow<DeviceEntity?>

    @Query("SELECT * FROM device_table LIMIT 1")
    suspend fun getDeviceOneShot(): DeviceEntity?

    // Ephemeral IDs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEphemeralId(id: EphemeralIdEntity)

    @Query("SELECT * FROM ephemeral_id_table ORDER BY generatedAt DESC LIMIT 1")
    suspend fun getCurrentEphemeralId(): EphemeralIdEntity?

    // Seen Peers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeenPeer(peer: SeenPeerEntity)

    @Query("SELECT * FROM seen_peers_table WHERE lastSeen > :minTimestamp")
    fun getActivePeers(minTimestamp: Long): Flow<List<SeenPeerEntity>>

    @Query("DELETE FROM seen_peers_table WHERE lastSeen < :timestamp")
    suspend fun cleanOldPeers(timestamp: Long)

    // User Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile_table WHERE rId = :rId")
    fun getUserProfile(rId: String): Flow<UserProfileEntity?>

    @Query("UPDATE user_profile_table SET name = :name WHERE rId = :rId")
    suspend fun updateUserName(rId: String, name: String)

    // Emergency Contacts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contact_table WHERE id = :id")
    suspend fun deleteContact(id: Int)

    @Query("SELECT * FROM emergency_contact_table WHERE ownerId = :ownerId")
    fun getContacts(ownerId: String): Flow<List<EmergencyContactEntity>>
}

@Database(entities = [DeviceEntity::class, EphemeralIdEntity::class, SeenPeerEntity::class, UserProfileEntity::class, EmergencyContactEntity::class], version = 2, exportSchema = false)
abstract class RahatDatabase : RoomDatabase() {
    abstract fun rahatDao(): RahatDao

    companion object {
        @Volatile
        private var INSTANCE: RahatDatabase? = null

        fun getDatabase(context: android.content.Context): RahatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RahatDatabase::class.java,
                    "rahat_database"
                )
                .fallbackToDestructiveMigration() // Dev only: Wipes data on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
