package com.rahat.data.repo

import android.content.Context
import com.rahat.security.IdentityManager
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class PeerResolver @Inject constructor(
    private val context: Context,
    private val identityManager: IdentityManager
) {
    private val nameCache = mutableMapOf<String, String>() // rId -> Name

    fun resolvePeerName(rId: String): String {
        return nameCache[rId] ?: "Rahat User" // Fallback: Backend sync logic would go here
    }

    fun updateCache(rId: String, name: String) {
        nameCache[rId] = name
    }
}
