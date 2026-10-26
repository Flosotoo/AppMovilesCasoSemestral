package com.example.teqmed.ui.supervisor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import java.util.Locale

/**
 * Bandeja de Supervisión Técnica y Resolución de Desvíos (HU26, HU27, HU28, RF-22, RF-23, RF-24).
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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

            // Selector de Bandeja (HU26: Pendientes vs Resueltos)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.verSoloPendientes) "Pendientes de Validación" else "Historial de Resueltos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.verSoloPendientes,
                        onClick = { viewModel.onFiltroPendientesChange(true) },
                        label = { Text("Pendientes") }
                    )
                    FilterChip(
                        selected = !state.verSoloPendientes,
                        onClick = { viewModel.onFiltroPendientesChange(false) },
                        label = { Text("Resueltos") }
                    )
                }
            }

            if (state.trasladosVisibles.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.verSoloPendientes)
                                "No hay traslados finalizados pendientes de validación en la bandeja."
                            else
                                "No se registran traslados históricos resueltos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.trasladosVisibles) { traslado ->
                        val destino = state.destinosMap[traslado.destinoId]
                        val tecnicoNombre = state.usuariosMap[traslado.tecnicoId] ?: "Técnico #${traslado.tecnicoId}"
                        val distDeclarada = if (traslado.odometroFinal != null) (traslado.odometroFinal - traslado.odometroInicial) else 0.0
                        val distOficial = destino?.distanciaOficialKm ?: 0.0
                        val deltaKm = distDeclarada - distOficial
                        val pctDelta = if (distOficial > 0.0) (deltaKm / distOficial) * 100.0 else 0.0
                        val signo = if (deltaKm > 0) "+" else ""

                        val hayDesviacion = traslado.desviacionDetectada

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = if (hayDesviacion) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = if (hayDesviacion)
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Cabecera: Destino y Estado
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = destino?.nombreInstitucion ?: "Destino #${traslado.destinoId}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
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
                                    text = "Técnico: $tecnicoNombre · Vehículo: ${traslado.vehiculoPatente} (${traslado.sentido})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))

                                // Comparador Exacto de Distancias (HU27, RF-23)
                                Text(
                                    text = "Auditoría de Distancias:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Declarada: ${String.format(Locale.US, "%.1f", distDeclarada)} km",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Oficial: ${String.format(Locale.US, "%.1f", distOficial)} km",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Diferencia: $signo${String.format(Locale.US, "%.1f", deltaKm)} km ($signo${String.format(Locale.US, "%.1f", pctDelta)}%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (hayDesviacion) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )

                                    if (hayDesviacion) {
                                        SuggestionChip(
                                            onClick = {},
                                            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                            label = {
                                                Text(
                                                    if (deltaKm > 0) "Exceso (>10% o >5 km)" else "Defecto (<-10% o <-5 km)",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        )
                                    }
                                }

                                traslado.costoTotal?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
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

                                // Si ya fue resuelto, mostrar comentario registrado
                                if (!traslado.comentarioResolucion.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "Resolución registrada (${traslado.decisionResolucion ?: ""}):",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = traslado.comentarioResolucion,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
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
