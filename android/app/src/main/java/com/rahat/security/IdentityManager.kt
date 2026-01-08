package com.rahat.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import android.content.SharedPreferences

class IdentityManager(private val context: Context) {

    private val KEY_ALIAS = "RahatDeviceSecret"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val PREFS_FILENAME = "rahat_secure_prefs"
    private val KEY_RID = "persistent_rid"
    
    companion object {
        const val TIME_WINDOW_MS = 600_000L // 10 Minutes
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            PREFS_FILENAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // 1. Persistent R_id (Immutable after sync)
    fun savePersistentRId(rId: String) {
        encryptedPrefs.edit().putString(KEY_RID, rId).apply()
    }

    fun getPersistentRId(): String? {
        return encryptedPrefs.getString(KEY_RID, null)
    }

    // 2. Resolve R_id from EphID (Rolling Window)
    fun isEphIdValidForDevice(ephId: String, secret: SecretKey, timeWindowMs: Long): Boolean {
        val current = generateEphemeralIdAtOffset(secret, timeWindowMs, 0)
        val previous = generateEphemeralIdAtOffset(secret, timeWindowMs, -1)
        return ephId == current || ephId == previous
    }

    // 3. Get or Create DeviceSecret
    @Synchronized
    fun getOrCreateDeviceSecret(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
                ANDROID_KEYSTORE
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN
                ).build()
            )
            return keyGenerator.generateKey()
        }

        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    // 4. Generate Ephemeral ID with Offset (Senior Architect: 128-bit Hardened ID)
    fun generateEphemeralIdAtOffset(secret: SecretKey, timeWindowMs: Long, windowOffset: Long): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secret)
        
        val timeStep = (System.currentTimeMillis() / timeWindowMs) + windowOffset
        val timeBytes = timeStep.toString().toByteArray(Charsets.UTF_8)
        
        val hash = mac.doFinal(timeBytes)
        // Take 16 bytes for 128-bit EphID as requested
        val truncated = hash.copyOfRange(0, 16)
        return truncated.joinToString("") { "%02x".format(it) }
    }
    
    // 5. Generate R_id (Legacy support)
    fun generateRId(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
