package com.rahat.service.nearby

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rahat.MainActivity
import com.rahat.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RahatMeshService : Service() {

    private val binder = LocalBinder()
    private var nearbyManager: NearbyManager? = null
    
    // Commands
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RID = "EXTRA_RID"
        const val EXTRA_LAT = "EXTRA_LAT"
        const val EXTRA_LNG = "EXTRA_LNG"
        const val EXTRA_SEV = "EXTRA_SEV"
    }

    inner class LocalBinder : Binder() {
        fun getService(): RahatMeshService = this@RahatMeshService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        nearbyManager = NearbyManager(this)
        startForegroundService()
        // Always scan
        nearbyManager?.startScanning()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val rId = intent.getStringExtra(EXTRA_RID) ?: ""
                val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
                val lng = intent.getDoubleExtra(EXTRA_LNG, 0.0)
                val sev = intent.getStringExtra(EXTRA_SEV) ?: "LOW"
                
                if (rId.isNotEmpty()) {
                    nearbyManager?.startAdvertisingLoop(rId, "User", lat, lng, sev)
                }
            }
            ACTION_STOP -> {
                nearbyManager?.stopAdvertising()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "RahatMeshChannel"
        val channelName = "Rahat Mesh Service"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rahat Mesh Active")
            .setContentText("Participating in the offline emergency network...")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        nearbyManager?.stopScanning()
        nearbyManager?.stopAdvertising()
    }
}
