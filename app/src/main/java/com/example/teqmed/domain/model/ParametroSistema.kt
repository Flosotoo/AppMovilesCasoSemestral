package com.example.teqmed.domain.model

/** Parametros vigentes del calculo (RF-16, RF-32). Un solo lugar los define. */
data class ParametroSistema(
    val id: Long = 0,
    val tipo: TipoParametro,
    val valor: Double,
    val fuenteReferencia: String = "",
    val fechaActualizacion: Long = System.currentTimeMillis()
) {
    fun valorValido(): Boolean = valor > 0
}

/** Los tres parametros juntos, tal como los consume la calculadora. */
data class TarifasVigentes(
    val precioLitroCne: Double,
    val tarifaReembolsoKm: Double,
    val umbralDesviacionPct: Double = 10.0,
    val fuentePrecio: String = "CNE / Energia Abierta",
    val fechaPrecio: Long = System.currentTimeMillis()
)