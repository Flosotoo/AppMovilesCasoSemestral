package com.example.teqmed.ui.trip

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.teqmed.util.CameraHelper
import com.example.teqmed.util.LocationHelper
import kotlinx.coroutines.launch

@Composable
fun CierreTrasladoScreen(
    viewModel: CierreTrasladoViewModel,
    onTrasladoFinalizado: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var peajeIdParaFoto by remember { mutableStateOf<String?>(null) }
    var indicePeajePendiente by remember { mutableIntStateOf(1) }
    var uriTempFoto by remember { mutableStateOf<Uri?>(null) }
    var mostrarDialogoPermisoCamara by remember { mutableStateOf(false) }
    var mostrarDialogoPermisoGps by remember { mutableStateOf(false) }

    // Helper GPS
    val locationHelper = remember { LocationHelper(context) }

    // Launcher nativo para cámara (FileProvider)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exitoso ->
        val id = peajeIdParaFoto
        val uri = uriTempFoto
        if (exitoso && id != null && uri != null) {
            viewModel.onFotoPeajeCapturada(id, uri.toString())
        }
    }

    val lanzarCamaraSegura: (String, Int) -> Unit = { idPeaje, numeroPeaje ->
        try {
            val uri = CameraHelper.crearUriTemporal(context, "peaje_${numeroPeaje}_")
            peajeIdParaFoto = idPeaje
            uriTempFoto = uri
            cameraLauncher.launch(uri)
        } catch (_: SecurityException) {
            mostrarDialogoPermisoCamara = true
        } catch (_: Exception) {
            mostrarDialogoPermisoCamara = true
        }
    }

    // Launcher de permiso de cámara en tiempo de ejecución
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val idPendiente = peajeIdParaFoto
        if (concedido && idPendiente != null) {
            lanzarCamaraSegura(idPendiente, indicePeajePendiente)
        } else if (!concedido) {
            mostrarDialogoPermisoCamara = true
        }
    }

    val capturarUbicacionFinSegura: () -> Unit = {
        scope.launch {
            val loc = locationHelper.obtenerUbicacionActual()
            if (loc != null) {
                viewModel.onUbicacionGpsCapturada(loc.latitude, loc.longitude)
            }
        }
    }

    // Launcher de permiso de ubicación en tiempo de ejecución
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            capturarUbicacionFinSegura()
        } else {
            mostrarDialogoPermisoGps = true
        }
    }

    LaunchedEffect(state.trasladoCerradoExitoso) {
        if (state.trasladoCerradoExitoso) {
            onTrasladoFinalizado()
        }
    }

    if (mostrarDialogoPermisoCamara) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisoCamara = false },
            title = { Text("Permiso de Cámara Requerido") },
            text = {
                Text("Para respaldar el cobro de peaje es necesario acceder a la cámara y fotografiar el comprobante físico. ¿Deseas conceder el permiso?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoPermisoCamara = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Permitir Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermisoCamara = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoPermisoGps) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisoGps = false },
            title = { Text("Permiso de Ubicación Requerido") },
            text = {
                Text("Para registrar la ubicación GPS de llegada al finalizar el traslado se requiere acceso a la ubicación del dispositivo.")
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
                    Text("Cancelar")
                }
            }
        )
    }

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
                text = "Finalizar Traslado en Terreno",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Tarjeta de Odómetro
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Odómetro Inicial: ${state.odometroInicial.toInt()} km",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SuggestionChip(
                            onClick = { },
                            label = { Text("${state.sentido.etiqueta} (Ref. ${state.distanciaOficialKm} km)") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.odometroFinal,
                        onValueChange = viewModel::onOdometroFinalChange,
                        label = { Text("Kilometraje Final (Odómetro)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = state.odometroFinalError != null,
                        supportingText = {
                            state.odometroFinalError?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    // Alerta reactiva de desvío y tramo conforme (HU20 -> RF-11)
                    if (state.alertaDesvio != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.desvioSuperaUmbral)
                                    MaterialTheme.colorScheme.errorContainer
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.alertaDesvio!!,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.desvioSuperaUmbral)
                                    MaterialTheme.colorScheme.onErrorContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Sección Dinámica de Peajes Múltiples (HU18)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Peajes en Ruta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        SuggestionChip(
                            onClick = { },
                            label = { Text("Total: $${state.totalPeajes}") }
                        )
                    }

                    if (state.peajes.isEmpty()) {
                        Text(
                            text = "Sin peajes registrados en este traslado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Lista de Peajes agregados dinámicamente
                    state.peajes.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Peaje #${index + 1}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { viewModel.eliminarPeaje(item.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar peaje",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = item.monto,
                                    onValueChange = { nuevoMonto ->
                                        viewModel.onMontoPeajeChange(item.id, nuevoMonto)
                                    },
                                    label = { Text("Monto Pagado ($ CLP)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = item.montoError != null,
                                    supportingText = {
                                        item.montoError?.let {
                                            Text(text = it, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                )

                                OutlinedButton(
                                    onClick = {
                                        peajeIdParaFoto = item.id
                                        indicePeajePendiente = index + 1
                                        if (CameraHelper.tienePermisoCamara(context)) {
                                            lanzarCamaraSegura(item.id, index + 1)
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (item.fotoUri == null) "Fotografiar Ticket #${index + 1}"
                                        else "Cambiar Foto Ticket #${index + 1}"
                                    )
                                }

                                if (item.fotoUri != null) {
                                    val bitmapPreview = remember(item.fotoUri) {
                                        CameraHelper.cargarBitmapDesdeUri(context, item.fotoUri)
                                    }
                                    if (bitmapPreview != null) {
                                        Image(
                                            bitmap = bitmapPreview,
                                            contentDescription = "Vista previa ticket de peaje #${index + 1}",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Comprobante fotográfico adjunto",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    item.fotoError?.let {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Botón para agregar peaje
                    OutlinedButton(
                        onClick = { viewModel.agregarPeaje() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Peaje")
                    }
                }
            }

            // Sello GPS Check-out
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sello GPS de Llegada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (state.latitudGpsFin != null && state.longitudGpsFin != null) {
                            Text(
                                text = "Lat: ${String.format("%.4f", state.latitudGpsFin)}, Lng: ${String.format("%.4f", state.longitudGpsFin)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Pendiente de captura",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (locationHelper.tienePermisoUbicacion()) {
                                capturarUbicacionFinSegura()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Capturar")
                    }
                }
            }

            // Error general si existe
            state.errorGeneral?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Finalizar
            Button(
                onClick = { viewModel.cerrarTraslado() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = state.formularioValido && !state.guardando
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Finalizar y Guardar Traslado", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
