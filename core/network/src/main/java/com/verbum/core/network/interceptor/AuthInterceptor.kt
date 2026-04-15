package com.verbum.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    @Volatile
    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val currentToken = token

        val request = if (currentToken != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        } else {
            original
        }

        Timber.d("→ ${request.method} ${request.url}")
        return chain.proceed(request)
    }
}
