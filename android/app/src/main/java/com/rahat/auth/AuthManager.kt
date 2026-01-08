package com.rahat.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.rahat.R

class AuthManager(private val context: Context, private val userSession: UserSession) {

    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null

    init {
        // Initialize MSAL
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            R.raw.auth_config, // Assumes res/raw/auth_config.json exists
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mSingleAccountApp = application
                    Log.d("AuthManager", "MSAL Initialized")
                }

                override fun onError(exception: MsalException) {
                    Log.e("AuthManager", "MSAL Init Error: $exception")
                }
            })
    }

    fun signIn(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (mSingleAccountApp == null) {
            onError("Auth not initialized. Config file missing?")
            return
        }

        mSingleAccountApp?.signIn(activity, null, SCOPES, object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                val account = authenticationResult.account
                val name = account.username // Or claims
                userSession.loginUser(account.id, name)
                onSuccess()
            }

            override fun onError(exception: MsalException) {
                Log.e("AuthManager", "Sign-in failed: $exception")
                onError(exception.message ?: "Login Failed")
            }

            override fun onCancel() {
                Log.d("AuthManager", "User cancelled login")
            }
        })
    }

    fun signOut(onComplete: () -> Unit) {
        mSingleAccountApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
                userSession.logout()
                onComplete()
            }

            override fun onError(exception: MsalException) {
                Log.e("AuthManager", "Sign-out error: $exception")
                // Force local logout anyway
                userSession.logout()
                onComplete()
            }
        })
    }
    
    companion object {
        private val SCOPES = arrayOf("User.Read")
    }
}
