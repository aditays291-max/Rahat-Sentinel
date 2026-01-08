package com.rahat

import androidx.multidex.MultiDexApplication

class MainApplication : MultiDexApplication() {
  override fun onCreate() {
    super.onCreate()
    // Initialize things here if needed (e.g. MSAL, Map config)
  }
}
