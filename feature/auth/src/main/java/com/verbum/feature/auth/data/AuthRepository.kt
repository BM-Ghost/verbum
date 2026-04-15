package com.verbum.feature.auth.data

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signOut()
    suspend fun isSignedIn(): Boolean
}
