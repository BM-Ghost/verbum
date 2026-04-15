package com.verbum.feature.auth.domain

import com.verbum.feature.auth.data.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email is required"))
        if (password.length < 6) return Result.failure(IllegalArgumentException("Password too short"))
        return authRepository.signIn(email, password)
    }
}
