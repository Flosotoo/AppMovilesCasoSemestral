package com.example.teqmed.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Contrato reactivo para observación del estado de conectividad (HU24, RF-19).
 */
interface NetworkMonitor {
    val isOnline: StateFlow<Boolean>
}

/**
 * Implementación defensiva en tiempo real sobre ConnectivityManager.
 * Zero crashes: ante cualquier excepción de permisos o hardware, degrada a offline de forma segura.
 */
class LiveNetworkMonitor(
    context: Context
) : NetworkMonitor {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(verificarConectividadInicial())
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        registrarObservador()
    }

    private fun verificarConectividadInicial(): Boolean {
        return try {
            val cm = connectivityManager ?: return false
            val redActiva = cm.activeNetwork ?: return false
            val capacidades = cm.getNetworkCapabilities(redActiva) ?: return false
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (_: Exception) {
            false
        }
    }

    private fun registrarObservador() {
        try {
            val cm = connectivityManager ?: return
            val peticion = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(peticion, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }

                override fun onLost(network: Network) {
                    _isOnline.value = verificarConectividadInicial()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val tieneInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    _isOnline.value = tieneInternet
                }
            })
        } catch (_: Exception) {
            // Protección contra caídas
        }
    }
}
