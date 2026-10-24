package com.example.teqmed.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

//Encapsula FusedLocationProviderClient para capturar sellos de presencia GPS puntual.
//Si el sensor falla o está apagado, retorna null sin provocar crashes.

class LocationHelper(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(): Location? {
        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener {
                    // ante fallos de hardware
                    continuation.resume(null)
                }
            continuation.invokeOnCancellation { cts.cancel() }
        }
    }
}
