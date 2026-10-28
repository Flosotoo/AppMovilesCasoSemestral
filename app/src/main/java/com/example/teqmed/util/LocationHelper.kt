package com.example.teqmed.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Encapsula FusedLocationProviderClient para capturar sellos de presencia GPS puntual.
// Verifica permisos en tiempo de ejecución y maneja excepciones sin provocar caídas.

class LocationHelper(context: Context) {
    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    fun tienePermisoUbicacion(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(): Location? {
        if (!tienePermisoUbicacion()) {
            return null
        }
        return try {
            suspendCancellableCoroutine { continuation ->
                val cts = CancellationTokenSource()
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            if (continuation.isActive) continuation.resume(location)
                        } else {
                            client.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    if (continuation.isActive) continuation.resume(lastLoc)
                                }
                                .addOnFailureListener {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                        }
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                continuation.invokeOnCancellation { cts.cancel() }
            }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }
}
