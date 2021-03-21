package com.piratmac.todo.use_cases


sealed class Result<out T, out E>
class Success<out T>(val value: T): Result<T, Nothing>()
class Failure<out E>(val error: E): Result<Nothing, E>()


fun <U, T, E> Result<T, E>.mapSuccess(transform: (T) -> U): Result<U, E> =
    when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

fun <U, T, E> Result<T, E>.mapError(transform: (E) -> U): Result<T, U> =
    when (this) {
        is Success -> this
        is Failure -> Failure(transform(error))
    }

fun <U, T, E> Result<T, E>.andThen(transform: (T) -> Result<U, E>): Result<U, E> =
    when (this) {
        is Success -> transform(value)
        is Failure -> this
    }
