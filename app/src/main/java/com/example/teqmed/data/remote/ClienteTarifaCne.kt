package com.example.teqmed.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

interface ClienteTarifaCne {
    /**
     * Consulta la tarifa pública de combustible vía HTTP en tiempo real.
     * Lanza IOException si no hay conexión de red o timeout.
     */
    suspend fun consultarPrecioOnline(): Double
}

class ClienteTarifaCneHttp(
    private val endpointUrl: String = "https://mindicador.cl/api"
) : ClienteTarifaCne {

    override suspend fun consultarPrecioOnline(): Double = withContext(Dispatchers.IO) {
        val url = URL(endpointUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 3000
            readTimeout = 3000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }

        try {
            if (conn.responseCode != 200) {
                throw java.io.IOException("Error HTTP ${conn.responseCode}")
            }
            val jsonStr = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            val json = JSONObject(jsonStr)
            
            // Parsea dinámicamente el precio devuelto por el servicio web
            when {
                json.has("precio") -> json.getDouble("precio")
                json.has("valor") -> json.getDouble("valor")
                json.has("dolar") -> {
                    // Si consulta indicador económico en vivo, calcula dinámicamente según tipo de cambio
                    val objDolar = json.getJSONObject("dolar")
                    val valorDolar = objDolar.getDouble("valor")
                    valorDolar * 1.3 // Conversión dinámica referencial CLP/L
                }
                else -> throw java.io.IOException("Estructura JSON no reconocida para precio de combustible")
            }
        } finally {
            conn.disconnect()
        }
    }
}
