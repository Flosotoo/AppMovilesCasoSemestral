package com.example.teqmed.ui.admin

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
 * Pantalla CRUD del Administrador para Destinos Clínicos (HU35, RF-31).
 */
@Composable
fun DestinosAdminScreen(
    viewModel: DestinosAdminViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::abrirDialogoCrear,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Destino") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Catálogo de Destinos Médicos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Distancias de referencia tabuladas desde Casa Matriz (Concepción)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(state.destinos) { destino ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = if (destino.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Column {
                                Text(
                                    text = destino.nombreInstitucion,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${destino.comuna} · Cliente: ${destino.nombreCliente}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Ref. Oficial: ${destino.distanciaOficialKm} km",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Switch de estado activo (HU37 / RF-33)
                        Switch(
                            checked = destino.activo,
                            onCheckedChange = { nuevoEstado ->
                                viewModel.cambiarEstadoActivo(destino.id, nuevoEstado)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal de Creación
    if (state.mostrandoDialogoCrear) {
        val form = state.formulario
        AlertDialog(
            onDismissRequest = viewModel::cerrarDialogoCrear,
            title = { Text("Registrar Nuevo Destino", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = form.nombreInstitucion,
                        onValueChange = viewModel::onFormInstitucionChange,
                        label = { Text("Nombre del Recinto") },
                        placeholder = { Text("Hospital Regional...") },
                        isError = form.nombreInstitucionError != null,
                        supportingText = { form.nombreInstitucionError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.nombreCliente,
                        onValueChange = viewModel::onFormClienteChange,
                        label = { Text("Nombre Cliente") },
                        placeholder = { Text("Servicio de Salud...") },
                        isError = form.nombreClienteError != null,
                        supportingText = { form.nombreClienteError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.comuna,
                        onValueChange = viewModel::onFormComunaChange,
                        label = { Text("Comuna") },
                        placeholder = { Text("Concepción, Coronel...") },
                        isError = form.comunaError != null,
                        supportingText = { form.comunaError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.distanciaOficialKm,
                        onValueChange = viewModel::onFormDistanciaChange,
                        label = { Text("Distancia Oficial (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = form.distanciaOficialKmError != null,
                        supportingText = { form.distanciaOficialKmError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = viewModel::guardarNuevoDestino) {
                    Text("Guardar Destino")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::cerrarDialogoCrear) {
                    Text("Cancelar")
                }
            }
        )
    }
}
