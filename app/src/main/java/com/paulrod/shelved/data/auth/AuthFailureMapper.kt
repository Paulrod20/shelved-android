package com.paulrod.shelved.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

internal fun Throwable.authFailure(): AuthFailure = when (this) {
    is FirebaseAuthWeakPasswordException -> AuthFailure.WEAK_PASSWORD
    is FirebaseAuthUserCollisionException -> AuthFailure.ACCOUNT_EXISTS
    is FirebaseAuthInvalidCredentialsException -> AuthFailure.INVALID_CREDENTIALS
    is FirebaseNetworkException -> AuthFailure.NETWORK
    else -> AuthFailure.UNKNOWN
}
