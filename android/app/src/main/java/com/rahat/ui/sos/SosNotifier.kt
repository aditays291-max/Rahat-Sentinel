package com.rahat.app.sos

import android.util.Log
import com.rahat.app.state.EmergencyContactStore

object SosNotifier {

    fun notifyContacts() {
        EmergencyContactStore.contacts.forEach { contact ->
            Log.d(
                "SOS_NOTIFY",
                "Notifying ${contact.name} at ${contact.phone}"
            )
        }
    }
}
