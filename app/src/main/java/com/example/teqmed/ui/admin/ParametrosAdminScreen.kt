package com.example.teqmed.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Pantalla de Configuración de Tarifas CNE y Umbrales de Desviación (HU19, HU36, RF-16, RF-32).
 * Cero validaciones en composables: delega a ParametrosAdminViewModel y ValidadorParametro.
 */
@Composable
fun ParametrosAdminScreen(
    viewModel: ParametrosAdminViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Tarifas y Parámetros Operacionales",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Configuración de tarifas de combustible y tolerancias de auditoría",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tarjeta Informativa de la Fuente Activa
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(
                        text = "Fuente activa del precio:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.fuentePrecio.ifBlank { "Base de datos local" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Formulario de Tarifas
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Valores de Cálculo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Campo Precio por Litro CNE
                OutlinedTextField(
                    value = state.precioLitro,
                    onValueChange = viewModel::onPrecioLitroChange,
                    label = { Text("Precio por Litro CNE ($/L)") },
                    placeholder = { Text("1240") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.precioLitroError != null,
                    supportingText = {
                        state.precioLitroError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        } ?: Text("Tarifa de referencia oficial para el cálculo de consumo")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo Umbral Porcentual
                OutlinedTextField(
                    value = state.umbralPct,
                    onValueChange = viewModel::onUmbralPctChange,
                    label = { Text("Umbral de Desviación Porcentual (%)") },
                    placeholder = { Text("10.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = state.umbralPctError != null,
                    supportingText = {
                        state.umbralPctError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        } ?: Text("Tolerancia porcentual admisible (1% a 100%)")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo Umbral en Km Absoluto
                OutlinedTextField(
                    value = state.umbralKm,
                    onValueChange = viewModel::onUmbralKmChange,
                    label = { Text("Umbral de Desviación Absoluta (km)") },
                    placeholder = { Text("5.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = state.umbralKmError != null,
                    supportingText = {
                        state.umbralKmError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        } ?: Text("Diferencia mínima en km para alertar desvío")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Mensaje de Éxito
        AnimatedVisibility(
            visible = state.mensajeExito != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            state.mensajeExito?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Mensaje de Error
        AnimatedVisibility(
            visible = state.errorGeneral != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            state.errorGeneral?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Botones de Acción
        Button(
            onClick = viewModel::guardarParametros,
            enabled = !state.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Guardar parámetros")
            }
        }

        OutlinedButton(
            onClick = viewModel::sincronizarConCneOnline,
            enabled = !state.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.CloudSync, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Sincronizar con CNE en línea")
        }
    }
}
