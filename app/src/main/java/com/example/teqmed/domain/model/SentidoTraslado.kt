package com.example.teqmed.domain.model

/**
 * Sentido operativo del traslado (RF-05 / HU10).
 *
 * - IDA: Desde Casa Matriz TEQMED (Concepción) hacia el recinto clínico.
 * - RETORNO: Desde el recinto clínico de regreso a Casa Matriz TEQMED (Concepción).
 *
 * Ambos tramos utilizan la distancia oficial tabulada del recinto clínico de forma simétrica
 * para la auditoría de desviaciones, evitando divisiones por cero o destinos artificiales de 0 km.
 */
enum class SentidoTraslado(val etiqueta: String) {
    IDA("Ida al Recinto"),
    RETORNO("Retorno a Base")
}
