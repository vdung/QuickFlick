package vdung.android.quickflick.data

import io.reactivex.FlowableTransformer

sealed class Result<T>(val value: T?) {
    data class Pending<T>(val previousResult: T?) : Result<T>(previousResult)
    data class Success<T>(val result: T) : Result<T>(result)
    data class Error<T>(val error: Throwable, val previousResult: T?) : Result<T>(previousResult)
}

fun <T> toResult(): FlowableTransformer<T, Result<T>> {
    return FlowableTransformer { upstream ->
        upstream
            .map { Result.Success(it) as Result<T> }
            .onErrorReturn { Result.Error(it, null) }
            .startWith(Result.Pending<T>(null))
    }
}