package com.rahat.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rahat.MainActivity
import com.rahat.R
import com.rahat.data.local.RahatDatabase
import javax.crypto.SecretKey
import com.rahat.data.repo.PeerResolver
import com.rahat.data.model.PeerState
import com.rahat.data.model.PeerSource
import com.rahat.service.ble.BleAdvertiser
import com.rahat.service.ble.BleScanner
import com.rahat.service.ble.PeerManager
import com.rahat.security.IdentityManager
import com.rahat.data.repo.MeshRepository
import kotlinx.coroutines.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent

/**
 * Senior Architect Implementation: EmergencyBleService
 * 
 * DESIGN PRINCIPLES:
 * 1. ADVERTISING STABILITY: Start once, stay alive. No redundant cycles.
 * 2. DECOUPLED ROTATION: EphID rotates every 10 mins without breaking BLE session.
 * 3. TRANSPARENCY: Mandatory audit logs for all orchestration events.
 */
class EmergencyBleService : Service() {

    private val TAG = "BLE_SERVICE"
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "rahat_ble_channel"
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private lateinit var advertiser: BleAdvertiser
    private lateinit var scanner: BleScanner
    private lateinit var peerManager: PeerManager
    private lateinit var identityManager: IdentityManager
    private lateinit var peerResolver: PeerResolver
    private lateinit var database: RahatDatabase
    
    // Rotation Settings
    private val ROTATION_INTERVAL_MS = IdentityManager.TIME_WINDOW_MS // 10 Minutes

    private var currentSeverity = 2 // Default to HIGH for testing emergency
    private var lastLat = 0.0
    private var lastLng = 0.0

    private var orchestrationJob: Job? = null
    
    private val bluetoothStateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, android.bluetooth.BluetoothAdapter.ERROR)
            when (state) {
                android.bluetooth.BluetoothAdapter.STATE_ON -> {
                    Log.i(TAG, "BLUETOOTH_STATE_ON: Re-activating Mesh Orchestration")
                    if (checkAllPermissions()) startMeshOrchestration()
                }
                android.bluetooth.BluetoothAdapter.STATE_OFF -> {
                    Log.w(TAG, "BLUETOOTH_STATE_OFF: Suspending Mesh Operations")
                    stopMeshOrchestration()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "BLE_SERVICE_CREATED: Initializing Architecture")
        
        database = RahatDatabase.getDatabase(this)
        identityManager = IdentityManager(this)
        peerResolver = PeerResolver(this, identityManager)
        
        // Initialize PeerManager (The Brain)
        peerManager = PeerManager(serviceScope)
        
        // Initialize BLE Comms
        advertiser = BleAdvertiser(this)
        scanner = BleScanner(this, peerManager)
        
        // Register Bluetooth State Monitor
        val filter = android.content.IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)
        
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "BLE_SERVICE_ON_START_COMMAND: Activating Mesh")
        
        intent?.let {
            lastLat = it.getDoubleExtra("lat", 0.0)
            lastLng = it.getDoubleExtra("lng", 0.0)
        }

        if (checkAllPermissions()) {
            startMeshOrchestration()
        } else {
            Log.e(TAG, "BLE_SERVICE_ABORTED: Fatal Permission Denied")
        }

        return START_STICKY
    }

    private fun startMeshOrchestration() {
        if (orchestrationJob?.isActive == true) return
        
        // 1. START SCANNING (Runs indefinitely)
        scanner.startScanning()

        // 2. START ADVERTISING & ROTATION LOOP
        orchestrationJob = serviceScope.launch {
            Log.i(TAG, "BLE_MESH_ORCHESTRATOR: Starting Persistent Advertising Session")
            while (isActive) {
                try {
                    val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                    if (adapter?.isEnabled == true) {
                        val secret = identityManager.getOrCreateDeviceSecret()
                        val selfEphId = identityManager.generateEphemeralIdAtOffset(secret, ROTATION_INTERVAL_MS, 0L)
                        
                        Log.i(TAG, "BLE_EPHID_ROTATION_EVENT: New ID: $selfEphId [Rotation Target: ${ROTATION_INTERVAL_MS/60000} mins]")
                        
                        // Update Advertiser (Will not restart if same, will use AdvertisingSet if active)
                        advertiser.startOrUpdateAdvertising(selfEphId, currentSeverity, false)
                    } else {
                        Log.w(TAG, "BLE_ORCHESTRATION_WAITING: Bluetooth is OFF")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "BLE_ORCHESTRATION_ERROR: ${e.message}")
                }
                
                // Sleep for the rotation interval or a bit less to stay ahead of window
                delay(ROTATION_INTERVAL_MS - 5000) 
            }
        }
    }

    private fun stopMeshOrchestration() {
        orchestrationJob?.cancel()
        scanner.stopScanning()
        advertiser.stopAdvertising()
    }

    private fun checkAllPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(android.Manifest.permission.BLUETOOTH)
            permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
        }
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)

        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Rahat BLE Network", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rahat Emergency Mesh")
            .setContentText("Mesh network active and secure")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "BLE_SERVICE_STOPPED: Tearing down Mesh")
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (e: Exception) {
            // Already unregistered or not registered
        }
        stopMeshOrchestration()
        serviceScope.cancel()
    }
}
