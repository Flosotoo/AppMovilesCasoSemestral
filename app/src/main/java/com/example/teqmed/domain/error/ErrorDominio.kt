package com.example.teqmed.domain.error
import com.example.teqmed.domain.model.EstadoTraslado

sealed class ErrorDominio(mensaje: String) : Exception(mensaje) {

    data class OdometroInvalido(val inicial: Double, val final: Double) :
        ErrorDominio("El odómetro final ($final) debe ser mayor al inicial ($inicial).")

    data class TarifaInvalida(val monto: Double) :
        ErrorDominio("El monto calculado ($monto) debe ser mayor que cero.")

    data class EstadoNoPermite(val estado: EstadoTraslado, val operacion: String) :
        ErrorDominio("No se puede $operacion un traslado en estado $estado.")

    object ComentarioRequerido :
        ErrorDominio("Observar o rechazar un traslado exige un comentario.")
}