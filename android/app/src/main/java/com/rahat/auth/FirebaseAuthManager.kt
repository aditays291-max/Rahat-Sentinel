package com.rahat.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class FirebaseAuthManager {
    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification (instant verification or auto-retrieval)
                signInWithCredential(credential, {}, onError)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@FirebaseAuthManager.verificationId = verificationId
                this@FirebaseAuthManager.resendToken = token
                onCodeSent()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    
    fun verifyOtp(
        code: String,
        onSuccess: (String) -> Unit,  // Returns Firebase UID
        onError: (String) -> Unit
    ) {
        val verificationId = this.verificationId
        if (verificationId == null) {
            onError("No verification ID found")
            return
        }
        
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential, onSuccess, onError)
    }
    
    private fun signInWithCredential(
        credential: PhoneAuthCredential,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onError("Failed to get user ID")
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Sign in failed")
            }
    }
    
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    fun getCurrentUserPhone(): String? {
        return auth.currentUser?.phoneNumber
    }
    
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    fun signOut() {
        auth.signOut()
    }
}
