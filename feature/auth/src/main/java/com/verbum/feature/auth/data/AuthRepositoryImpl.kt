package com.verbum.feature.auth.data

import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        // TODO: Wire to Firebase Auth / backend auth service
        return Result.success(Unit)
    }

    override suspend fun signOut() {
        // TODO: Clear token + session
    }

    override suspend fun isSignedIn(): Boolean {
        // TODO: Check persisted token
        return false
    }
}
