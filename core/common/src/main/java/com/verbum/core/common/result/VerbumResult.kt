package com.verbum.core.common.result

/**
 * Sealed interface representing the result of an operation.
 * Used across all layers to communicate success/failure without exceptions.
 */
sealed interface VerbumResult<out T> {
    data class Success<T>(val data: T) : VerbumResult<T>
    data class Error(val exception: Throwable, val message: String? = null) : VerbumResult<Nothing>
    data object Loading : VerbumResult<Nothing>
}

fun <T> VerbumResult<T>.successOrNull(): T? = when (this) {
    is VerbumResult.Success -> data
    else -> null
}

fun <T> VerbumResult<T>.errorMessageOrNull(): String? = when (this) {
    is VerbumResult.Error -> message ?: exception.localizedMessage
    else -> null
}

suspend fun <T> safeCall(block: suspend () -> T): VerbumResult<T> {
    return try {
        VerbumResult.Success(block())
    } catch (e: Exception) {
        VerbumResult.Error(e)
    }
}
