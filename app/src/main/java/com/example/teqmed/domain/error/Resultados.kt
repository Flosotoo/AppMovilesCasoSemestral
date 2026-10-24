package com.example.teqmed.domain.error

/**
 * Puentes entre Result<T> y ErrorDominio.
 * Se dejan aqui para que ningun ViewModel tenga que hacer casteos.
 */

/** Atajo para fallar con un error tipado. */
fun <T> fallar(error: ErrorDominio): Result<T> = Result.failure(error)

/**
 * Normaliza cualquier Throwable a ErrorDominio.
 * Garantiza que el ViewModel siempre tenga un mensaje en castellano.
 */
fun Throwable.aErrorDominio(): ErrorDominio = when (this) {
    is ErrorDominio -> this
    else -> ErrorDominio.Inesperado(this)
}

/** El mensaje listo para el UiState. Nunca devuelve null ni texto tecnico. */
fun Throwable.mensajeParaUsuario(): String = aErrorDominio().mensajeUsuario

/**
 * Envuelve un bloque que puede lanzar y devuelve siempre un Result
 * con error tipado. Es el reemplazo del try/catch repetido.
 */
inline fun <T> ejecutarSeguro(bloque: () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: Throwable) {
        Result.failure(e.aErrorDominio())
    }