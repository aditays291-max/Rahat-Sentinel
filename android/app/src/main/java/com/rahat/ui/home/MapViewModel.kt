package com.rahat.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahat.data.model.Alert
import com.rahat.data.repo.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

import com.rahat.auth.UserSession
import com.rahat.data.firebase.FirestoreUserRepository
import com.rahat.data.repo.MeshRepository
import com.rahat.data.model.PeerState

class MapViewModel(
    private val repository: AlertRepository,
    private val userRepo: FirestoreUserRepository
) : ViewModel() {

    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    val nearbyPeers: StateFlow<List<PeerState>> = MeshRepository.nearbyPeers

    fun onLocationUpdated(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        
        // If first location update, generate seeds
        if (_userLocation.value == null) {
            viewModelScope.launch {
                repository.generateDemoSeeds(location.latitude, location.longitude)
                _alerts.value = repository.getAlerts()
            }
        }
        
        _userLocation.value = geoPoint
        
        // Sync to Firestore for rescue (Using generic ID for now, or move logic to Service)
        // Ignoring sync here as Service handles mesh location.
    }
}
