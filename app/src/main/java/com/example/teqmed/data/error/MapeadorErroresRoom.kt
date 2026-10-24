package com.example.teqmed.data.error

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import com.example.teqmed.domain.error.ErrorDominio

/**
 * Traduce las excepciones de Room/SQLite a ErrorDominio.
 * Vive en data/ porque domain/ no debe conocer Android.
 */
object MapeadorErroresRoom {

    fun mapear(e: Throwable, valorEnConflicto: String = ""): ErrorDominio = when (e) {
        is ErrorDominio -> e
        is SQLiteConstraintException -> ErrorDominio.Datos.ClaveDuplicada(valorEnConflicto)
        is SQLiteException -> ErrorDominio.Datos.FalloEscritura(e)
        else -> ErrorDominio.Inesperado(e)
    }
}

/** Version con receiver para usarla dentro de los repositorios. */
inline fun <T> ejecutarEnRoom(
    valorEnConflicto: String = "",
    bloque: () -> T
): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: Throwable) {
        Result.failure(MapeadorErroresRoom.mapear(e, valorEnConflicto))
    }