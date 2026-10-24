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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
 * Pantalla CRUD del Administrador para la Flota Vehicular (HU34, RF-30).
 */
@Composable
fun VehiculosAdminScreen(
    viewModel: VehiculosAdminViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::abrirDialogoCrear,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Vehículo") }
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
                    text = "Catálogo de Flota Vehicular",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Móviles habilitados con su rendimiento base configurado (km/L)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(state.vehiculos) { vehiculo ->
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
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = if (vehiculo.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Column {
                                Text(
                                    text = "${vehiculo.patente} · ${vehiculo.marca} ${vehiculo.modelo}",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Propiedad: ${vehiculo.propiedad} · Combustible: ${vehiculo.tipoCombustible}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Rendimiento: ${vehiculo.rendimientoKmL} km/L",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Switch de baja lógica (HU37 / RF-33)
                        Switch(
                            checked = vehiculo.activo,
                            onCheckedChange = { nuevoEstado ->
                                viewModel.cambiarEstadoActivo(vehiculo.patente, nuevoEstado)
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
            title = { Text("Registrar Nuevo Vehículo", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = form.patente,
                        onValueChange = viewModel::onFormPatenteChange,
                        label = { Text("Patente") },
                        placeholder = { Text("KJTR45") },
                        isError = form.patenteError != null,
                        supportingText = { form.patenteError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.marca,
                        onValueChange = viewModel::onFormMarcaChange,
                        label = { Text("Marca") },
                        placeholder = { Text("Toyota, Chevrolet...") },
                        isError = form.marcaError != null,
                        supportingText = { form.marcaError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.modelo,
                        onValueChange = viewModel::onFormModeloChange,
                        label = { Text("Modelo") },
                        placeholder = { Text("Hilux, D-Max...") },
                        isError = form.modeloError != null,
                        supportingText = { form.modeloError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Propiedad del Vehículo:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = form.propiedad == "EMPRESA",
                            onClick = { viewModel.onFormPropiedadChange("EMPRESA") },
                            label = { Text("Empresa") }
                        )
                        FilterChip(
                            selected = form.propiedad == "PARTICULAR",
                            onClick = { viewModel.onFormPropiedadChange("PARTICULAR") },
                            label = { Text("Particular") }
                        )
                    }

                    OutlinedTextField(
                        value = form.rendimientoKmL,
                        onValueChange = viewModel::onFormRendimientoChange,
                        label = { Text("Rendimiento Base (km/L)") },
                        placeholder = { Text("12.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = form.rendimientoKmLError != null,
                        supportingText = { form.rendimientoKmLError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = viewModel::guardarNuevoVehiculo) {
                    Text("Guardar Vehículo")
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
