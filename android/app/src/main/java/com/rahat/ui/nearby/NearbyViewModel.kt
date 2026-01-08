package com.rahat.ui.nearby

import androidx.lifecycle.ViewModel
import com.rahat.data.repo.MeshRepository
import kotlinx.coroutines.flow.StateFlow

class NearbyViewModel : ViewModel() {
    val nearbyDevices = MeshRepository.nearbyPeers

    // Service manages scanning now.
    // We can expose methods to start/stop "Broadcasting" via Service Intent if needed,
    // but the UI just observes for now.
}
