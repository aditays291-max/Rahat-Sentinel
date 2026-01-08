package com.rahat.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rahat.auth.UserSession
import com.rahat.data.firebase.FirestoreUserRepository
import com.rahat.data.repo.AlertRepository

class MapViewModelFactory(
    private val repository: AlertRepository,
    private val userRepo: FirestoreUserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository, userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
