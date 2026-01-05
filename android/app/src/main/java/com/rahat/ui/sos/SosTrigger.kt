package com.rahat.app.ui.sos

import android.content.Context
import android.content.Intent
import com.rahat.app.MainActivity

object SosTrigger {

    fun trigger(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TRIGGER_SOS", true)
        }
        context.startActivity(intent)
    }
}
