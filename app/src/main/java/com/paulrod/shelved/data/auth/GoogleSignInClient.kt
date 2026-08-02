package com.paulrod.shelved.data.auth

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.paulrod.shelved.R
import java.security.SecureRandom

class GoogleSignInClient(applicationContext: Context) {
    private val appContext = applicationContext.applicationContext
    private val credentialManager = CredentialManager.create(appContext)

    suspend fun getIdToken(activityContext: Context): String {
        val googleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = appContext.getString(R.string.default_web_client_id),
        )
            .setNonce(randomNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
        val credential = credentialManager.getCredential(activityContext, request).credential

        check(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
        ) { "Google returned an unsupported credential." }

        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    private fun randomNonce(): String {
        val bytes = ByteArray(32).also(SecureRandom()::nextBytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}
