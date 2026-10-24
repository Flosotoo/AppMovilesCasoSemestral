package com.example.teqmed.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Diálogo de consentimiento informado para el uso de datos de geolocalización (HU08, RF-38).
 */
@Composable
fun ConsentimientoGpsDialog(
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onRechazar,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "GPS Consent",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Uso de Datos de Ubicación",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "TeqRuta recopila datos de ubicación exclusivamente como sello de auditoría de presencia en los momentos puntuales de:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Inicio de Traslado (Check-in en terreno)\n• Cierre de Traslado (Check-out en recinto)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "La aplicación NO realiza rastreo en ruta ni monitoreo continuo en segundo plano, protegiendo la privacidad del personal técnico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onAceptar) {
                Text("Comprendo y Acepto")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onRechazar) {
                Text("Ahora no")
            }
        }
    )
}
