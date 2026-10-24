package com.example.teqmed.domain.model

/** Foto del ticket de peaje (RN04, RF-08). Uri content:// del FileProvider. */
data class EvidenciaPeaje(
    val fotoUriLocal: String,
    val timestampCaptura: Long = System.currentTimeMillis()
) {
    fun existe(): Boolean = fotoUriLocal.isNotBlank()
}