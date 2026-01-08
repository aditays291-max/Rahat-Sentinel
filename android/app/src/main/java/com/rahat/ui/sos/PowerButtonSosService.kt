package com.rahat.ui.sos

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.rahat.MainActivity

class PowerButtonSosService : AccessibilityService() {

    private var pressCount = 0
    private val handler = Handler(Looper.getMainLooper())

    private val resetRunnable = Runnable {
        pressCount = 0
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            pressCount++

            handler.removeCallbacks(resetRunnable)
            handler.postDelayed(resetRunnable, 3000) // 3 sec window

            // 🚨 POWER BUTTON pressed 3 times
            if (pressCount >= 3) {
                triggerSOS()
                pressCount = 0
            }
        }
    }

    private fun triggerSOS() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra("TRIGGER_SOS", true)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
