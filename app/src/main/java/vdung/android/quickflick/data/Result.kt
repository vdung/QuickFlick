package vdung.android.quickflick.data

sealed class Result<T>(val value: T) {
    data class Pending<T>(val previousResult: T) : Result<T>(previousResult)
    data class Success<T>(val result: T) : Result<T>(result)
    data class Error<T>(val error: Throwable, val previousResult: T) : Result<T>(previousResult)
}