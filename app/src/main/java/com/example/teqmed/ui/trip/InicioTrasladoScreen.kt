package com.example.teqmed.ui.trip

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.teqmed.domain.model.SentidoTraslado
import com.example.teqmed.util.LocationHelper
import kotlinx.coroutines.launch

/**
 * Pantalla de registro de inicio de traslado en terreno (HU10, HU11).
 * Cero validación en la interfaz: todas residen en ValidadorTraslado y son expuestas por el ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioTrasladoScreen(
    viewModel: InicioTrasladoViewModel,
    onTrasladoIniciado: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }

    var expandedDestino by remember { mutableStateOf(false) }
    var expandedVehiculo by remember { mutableStateOf(false) }
    var mostrarDialogoPermisoGps by remember { mutableStateOf(false) }

    val capturarUbicacionSegura: () -> Unit = {
        scope.launch {
            val location = locationHelper.obtenerUbicacionActual()
            if (location != null) {
                viewModel.onUbicacionCapturada(location.latitude, location.longitude)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            capturarUbicacionSegura()
        } else {
            mostrarDialogoPermisoGps = true
        }
    }

    val solicitarOCapturarGps: () -> Unit = {
        if (locationHelper.tienePermisoUbicacion()) {
            capturarUbicacionSegura()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Captura inicial defensiva de ubicación al ingresar a la pantalla (HU11)
    LaunchedEffect(Unit) {
        if (locationHelper.tienePermisoUbicacion()) {
            capturarUbicacionSegura()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(state.trasladoIniciadoId) {
        state.trasladoIniciadoId?.let { id ->
            onTrasladoIniciado(id)
        }
    }

    if (mostrarDialogoPermisoGps) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisoGps = false },
            title = { Text("Permiso de Ubicación Requerido") },
            text = {
                Text("Para adjuntar la ubicación GPS al iniciar el traslado es necesario permitir el acceso a la ubicación del dispositivo.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoPermisoGps = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Solicitar Permiso")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermisoGps = false }) {
                    Text("Continuar sin GPS")
                }
            }
        )
    }

    val destinoSeleccionado = state.destinosDisponibles.find { it.id == state.destinoSeleccionadoId }
    val vehiculoSeleccionado = state.vehiculosDisponibles.find { it.patente == state.vehiculoSeleccionadoPatente }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Iniciar Traslado en Terreno",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Sello GPS de Presencia (HU11)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = { solicitarOCapturarGps() },
                    icon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "GPS",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (state.latitudGpsInicio != null) {
                                "GPS: %.4f, %.4f".format(state.latitudGpsInicio, state.longitudGpsInicio)
                            } else "Capturar GPS Inicio"
                        )
                    }
                )
            }

            // 1. Selector de Sentido de Tramo (Ida / Retorno)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sentido Operativo del Tramo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = state.sentido == SentidoTraslado.IDA,
                            onClick = { viewModel.onSentidoChange(SentidoTraslado.IDA) },
                            label = { Text("Ida al Recinto") }
                        )
                        FilterChip(
                            selected = state.sentido == SentidoTraslado.RETORNO,
                            onClick = { viewModel.onSentidoChange(SentidoTraslado.RETORNO) },
                            label = { Text("Retorno a Base (Concepción)") }
                        )
                    }
                }
            }

            // 2. Selección de Destino / Recinto Clínico
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Centro de Salud de Destino",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedDestino,
                        onExpandedChange = { expandedDestino = !expandedDestino }
                    ) {
                        OutlinedTextField(
                            value = destinoSeleccionado?.let { "${it.nombreInstitucion} (${it.distanciaOficialKm} km)" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Seleccione Centro Médico") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDestino) },
                            isError = state.destinoError != null,
                            supportingText = {
                                state.destinoError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDestino,
                            onDismissRequest = { expandedDestino = false }
                        ) {
                            state.destinosDisponibles.forEach { destino ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(destino.nombreInstitucion, fontWeight = FontWeight.Medium)
                                            Text(
                                                "${destino.comuna} · Ref: ${destino.distanciaOficialKm} km",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.onDestinoChange(destino.id)
                                        expandedDestino = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Selección de Vehículo Asignado
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vehículo Asignado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedVehiculo,
                        onExpandedChange = { expandedVehiculo = !expandedVehiculo }
                    ) {
                        OutlinedTextField(
                            value = vehiculoSeleccionado?.let { "${it.patente} - ${it.marca} ${it.modelo} (${it.propiedad})" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Seleccione Móvil de la Flota") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehiculo) },
                            isError = state.vehiculoError != null,
                            supportingText = {
                                state.vehiculoError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedVehiculo,
                            onDismissRequest = { expandedVehiculo = false }
                        ) {
                            state.vehiculosDisponibles.forEach { vehiculo ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${vehiculo.patente} · ${vehiculo.marca} ${vehiculo.modelo}", fontWeight = FontWeight.Medium)
                                            Text(
                                                "${vehiculo.propiedad} · Rendimiento: ${vehiculo.rendimientoKmL} km/L",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.onVehiculoChange(vehiculo.patente)
                                        expandedVehiculo = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Kilometraje Odómetro Inicial
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kilometraje del Tablero (Inicio)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.odometroInicial,
                        onValueChange = viewModel::onOdometroChange,
                        label = { Text("Odómetro Inicial (km)") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = state.odometroInicialError != null,
                        supportingText = {
                            state.odometroInicialError?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(
                visible = state.errorGeneral != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.errorGeneral?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 5. Botón de Confirmación
            Button(
                onClick = viewModel::iniciarTraslado,
                enabled = !state.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Comenzar Traslado")
                }
            }
        }
    }
}
