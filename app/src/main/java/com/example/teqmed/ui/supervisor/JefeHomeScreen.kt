package com.example.teqmed.ui.supervisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqmed.domain.model.DecisionValidacion
import com.example.teqmed.domain.model.EstadoTraslado

/**
 * Bandeja de Supervisión y Resolución de Jefatura (HU26, HU27, HU28).
 */
@Composable
fun JefeHomeScreen(
    jefeNombre: String,
    viewModel: JefeHomeViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Bandeja de Supervisión Técnica",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Auditoría de traslados y resolución de desvíos — $jefeNombre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.traslados.isEmpty()) {
                Text(
                    text = "No hay traslados registrados en el sistema.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.traslados) { traslado ->
                        val destino = state.destinosMap[traslado.destinoId]
                        val distDeclarada = if (traslado.odometroFinal != null) (traslado.odometroFinal - traslado.odometroInicial).toInt() else 0
                        val distOficial = destino?.distanciaOficialKm ?: 0.0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = destino?.nombreInstitucion ?: "Destino #${traslado.destinoId}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = traslado.estado,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (traslado.estado) {
                                            EstadoTraslado.VALIDADO.name -> MaterialTheme.colorScheme.primary
                                            EstadoTraslado.OBSERVADO.name, EstadoTraslado.RECHAZADO.name -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sentido: ${traslado.sentido} · Móvil: ${traslado.vehiculoPatente}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Comparador de Distancias y Alerta de Desviación (HU27, RF-23)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Distancia: $distDeclarada km (Oficial: $distOficial km)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (traslado.desviacionDetectada) {
                                        SuggestionChip(
                                            onClick = {},
                                            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                            label = { Text("Desvío Detectado", color = MaterialTheme.colorScheme.error) }
                                        )
                                    }
                                }

                                traslado.costoTotal?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Costo Total: $%,d CLP (Peajes: $%,d CLP)".format(it.toInt(), traslado.montoPeajes),
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (traslado.estado == EstadoTraslado.FINALIZADO.name || traslado.estado == EstadoTraslado.OBSERVADO.name) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.abrirDialogoResolucion(traslado) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.RateReview, contentDescription = null)
                                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                        Text("Evaluar y Resolver Traslado")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Resolución de Jefatura (HU28, RF-24)
    state.trasladoParaResolver?.let { traslado ->
        AlertDialog(
            onDismissRequest = viewModel::cerrarDialogoResolucion,
            title = { Text("Resolución del Traslado", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Seleccione la decisión técnica para este registro de traslado:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.decisionSeleccionada == DecisionValidacion.VALIDADO,
                            onClick = { viewModel.onDecisionChange(DecisionValidacion.VALIDADO) },
                            label = { Text("Validar") }
                        )
                        FilterChip(
                            selected = state.decisionSeleccionada == DecisionValidacion.OBSERVADO,
                            onClick = { viewModel.onDecisionChange(DecisionValidacion.OBSERVADO) },
                            label = { Text("Observar") }
                        )
                        FilterChip(
                            selected = state.decisionSeleccionada == DecisionValidacion.RECHAZADO,
                            onClick = { viewModel.onDecisionChange(DecisionValidacion.RECHAZADO) },
                            label = { Text("Rechazar") }
                        )
                    }

                    OutlinedTextField(
                        value = state.comentario,
                        onValueChange = viewModel::onComentarioChange,
                        label = {
                            Text(
                                if (state.decisionSeleccionada == DecisionValidacion.VALIDADO)
                                    "Comentario (Opcional)"
                                else
                                    "Motivo de la Observación o Rechazo (Obligatorio)"
                            )
                        },
                        isError = state.comentarioError != null,
                        supportingText = {
                            state.comentarioError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::resolverTraslado,
                    enabled = !state.guardando
                ) {
                    Text("Confirmar Resolución")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::cerrarDialogoResolucion) {
                    Text("Cancelar")
                }
            }
        )
    }
}
