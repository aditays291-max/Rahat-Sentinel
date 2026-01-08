package com.rahat.data.repo

import com.rahat.data.model.PeerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
object MeshRepository {
    private val _nearbyPeers = MutableStateFlow<List<PeerState>>(emptyList())
    val nearbyPeers: StateFlow<List<PeerState>> = _nearbyPeers.asStateFlow()

    fun updatePeer(peer: PeerState) {
        val currentList = _nearbyPeers.value.toMutableList()
        val index = currentList.indexOfFirst { it.rId == peer.rId }
        
        if (index != -1) {
            currentList[index] = peer
        } else {
            currentList.add(peer)
        }
        
        _nearbyPeers.value = currentList.sortedByDescending { it.lastSeen }
    }

    fun setPeers(peers: List<PeerState>) {
        _nearbyPeers.value = peers.sortedByDescending { it.lastSeen }
    }
}
