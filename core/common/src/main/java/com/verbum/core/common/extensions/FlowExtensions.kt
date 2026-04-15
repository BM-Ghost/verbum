package com.verbum.core.common.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import com.verbum.core.common.result.VerbumResult
import timber.log.Timber

fun <T> Flow<T>.asResult(): Flow<VerbumResult<T>> {
    return this
        .map<T, VerbumResult<T>> { VerbumResult.Success(it) }
        .catch {
            Timber.e(it, "Flow error")
            emit(VerbumResult.Error(it))
        }
}
