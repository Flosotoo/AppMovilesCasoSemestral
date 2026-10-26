package com.example.teqmed.ui.trip

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqmed.data.local.DestinoEntity
import com.example.teqmed.data.local.TrasladoEntity
import com.example.teqmed.domain.model.EstadoTraslado
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Hub principal del Técnico de Servicio en terreno (HU09, HU10, HU12, HU13, HU23).
 */
@Composable
fun TecnicoHomeScreen(
    tecnicoNombre: String,
    trasladosFlow: Flow<List<TrasladoEntity>>,
    destinosMap: Map<Long, DestinoEntity>,
    onIniciarNuevoTraslado: () -> Unit,
    onFinalizarTraslado: (TrasladoEntity) -> Unit
) {
    val traslados by trasladosFlow.collectAsState(initial = emptyList())
    val trasladoEnCurso = traslados.find { it.estado == EstadoTraslado.EN_CURSO.name }

    // Control de vista: Jornada de Hoy (HU09) vs Historial Acumulado
    var verSoloHoy by remember { mutableStateOf(true) }
    var trasladoExpandidoId by remember { mutableStateOf<String?>(null) }

    val inicioDiaEpoch = remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }

    val trasladosFiltrados = remember(traslados, verSoloHoy, inicioDiaEpoch) {
        val noActivos = traslados.filter { it.estado != EstadoTraslado.EN_CURSO.name }
        if (verSoloHoy) {
            noActivos.filter { it.timestampInicio >= inicioDiaEpoch }
        } else {
            noActivos
        }
    }

    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formatoCompleto = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bienvenida y Aviso GPS (HU08, RF-38)
            var mostrarAvisoGps by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hola, $tecnicoNombre",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Panel Operativo de Terreno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { mostrarAvisoGps = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Aviso de Auditoría GPS",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (mostrarAvisoGps) {
                com.example.teqmed.ui.auth.ConsentimientoGpsDialog(
                    onAceptar = { mostrarAvisoGps = false },
                    onRechazar = { mostrarAvisoGps = false }
                )
            }

            // 1. Tarjeta de Traslado en Curso
            if (trasladoEnCurso != null) {
                val destino = destinosMap[trasladoEnCurso.destinoId]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRASLADO EN CURSO",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(trasladoEnCurso.sentido) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = destino?.nombreInstitucion ?: "Recinto ID #${trasladoEnCurso.destinoId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vehículo: ${trasladoEnCurso.vehiculoPatente} · Odómetro Inicial: ${trasladoEnCurso.odometroInicial.toInt()} km",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onFinalizarTraslado(trasladoEnCurso) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Cerrar y Rendir Traslado")
                        }
                    }
                }
            } else {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tienes traslados activos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Inicia un nuevo viaje al partir desde Concepción",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onIniciarNuevoTraslado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Nuevo Traslado")
                        }
                    }
                }
            }

            // 2. Encabezado de Jornada e Historial (HU09)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (verSoloHoy) "Jornada de Hoy" else "Historial Completo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = verSoloHoy,
                        onClick = { verSoloHoy = true },
                        label = { Text("Hoy") }
                    )
                    FilterChip(
                        selected = !verSoloHoy,
                        onClick = { verSoloHoy = false },
                        label = { Text("Todos") }
                    )
                }
            }

            // Lista de Traslados (HU09, HU13, HU23)
            if (trasladosFiltrados.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (verSoloHoy)
                                "No has registrado traslados en la jornada de hoy."
                            else
                                "No tienes traslados históricos registrados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(trasladosFiltrados) { item ->
                        val destinoItem = destinosMap[item.destinoId]
                        val estaExpandido = trasladoExpandidoId == item.id
                        val km = if (item.odometroFinal != null) (item.odometroFinal - item.odometroInicial).toInt() else 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable {
                                    trasladoExpandidoId = if (estaExpandido) null else item.id
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Fila Superior: Destino + Badges de Sync y Estado
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = destinoItem?.nombreInstitucion ?: "Destino #${item.destinoId}",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Badge de Sincronización (HU23, RF-20)
                                        if (item.estadoSincronizacion == "SINCRONIZADO") {
                                            Icon(
                                                imageVector = Icons.Default.CloudDone,
                                                contentDescription = "Sincronizado",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Sincronizado",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CloudOff,
                                                contentDescription = "Pendiente",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Pendiente",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        Text(
                                            text = "·",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )

                                        Text(
                                            text = item.estado,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (item.estado) {
                                                EstadoTraslado.VALIDADO.name -> MaterialTheme.colorScheme.primary
                                                EstadoTraslado.OBSERVADO.name, EstadoTraslado.RECHAZADO.name -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.outline
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Fila Intermedia: Sentido, Hora del día (HU09 CA1), Km y Costo
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val horaTexto = formatoHora.format(java.util.Date(item.timestampInicio))
                                    Text(
                                        text = "${item.sentido} · $horaTexto hrs · $km km",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    item.costoTotal?.let {
                                        Text(
                                            text = "$%,d CLP".format(it.toInt()),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Indicador de expansión animada (HU13)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        imageVector = if (estaExpandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (estaExpandido) "Ver menos" else "Ver detalle",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Detalle Expandible Completo (HU13, RF-20)
                                if (estaExpandido) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Detalle del Traslado",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Text(
                                            text = "Cliente: ${destinoItem?.nombreCliente ?: "N/A"} (${destinoItem?.comuna ?: "N/A"})",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Vehículo: ${item.vehiculoPatente}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Inicio: ${formatoCompleto.format(java.util.Date(item.timestampInicio))}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        item.timestampFin?.let { fin ->
                                            Text(
                                                text = "Cierre: ${formatoCompleto.format(java.util.Date(fin))}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Text(
                                            text = "Odómetro: ${item.odometroInicial.toInt()} km → ${item.odometroFinal?.toInt() ?: "-"} km (Declarada: $km km / Oficial: ${destinoItem?.distanciaOficialKm ?: 0.0} km)",
                                            style = MaterialTheme.typography.bodySmall
                                        )

                                        // Sellos GPS de Presencia (HU11)
                                        val gpsInicioTexto = if (item.latitudInicio != null && item.longitudInicio != null)
                                            String.format(Locale.US, "%.4f, %.4f", item.latitudInicio, item.longitudInicio)
                                        else
                                            "Sin ubicación"
                                        val gpsFinTexto = if (item.latitudFin != null && item.longitudFin != null)
                                            String.format(Locale.US, "%.4f, %.4f", item.latitudFin, item.longitudFin)
                                        else
                                            "Sin ubicación"

                                        Text(
                                            text = "GPS Check-in: $gpsInicioTexto · GPS Check-out: $gpsFinTexto",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        // Desglose Financiero Congelado (RF-15, RF-17)
                                        Text(
                                            text = "Desglose Financiero:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "• Precio CNE aplicado: ${item.precioLitroAplicado?.let { "$%,d CLP/L".format(it.toInt()) } ?: "N/A"}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• Peajes declarados: $%,d CLP".format(item.montoPeajes),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• Costo total imputado: $%,d CLP".format((item.costoTotal ?: 0.0).toInt()),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        // Alerta de Desviación
                                        if (item.desviacionDetectada) {
                                            SuggestionChip(
                                                onClick = {},
                                                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                                label = {
                                                    Text(
                                                        "Desviación Detectada (${item.porcentajeDesviacion?.let { "%.1f%%".format(it) } ?: ""})",
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            )
                                        }

                                        // Comentario de Resolución Jefatura (si existe)
                                        item.decisionResolucion?.let { decision ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(
                                                        text = "Resolución de Jefatura: $decision",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (!item.comentarioResolucion.isNullOrBlank()) {
                                                        Text(
                                                            text = item.comentarioResolucion,
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
            }
        }
    }
}
