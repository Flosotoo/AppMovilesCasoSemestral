package com.example.teqmed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.teqmed.data.local.DatosIniciales
import com.example.teqmed.data.local.DestinoEntity
import com.example.teqmed.data.local.SessionManager
import com.example.teqmed.data.local.TeqRutaDatabase
import com.example.teqmed.data.local.TrasladoEntity
import com.example.teqmed.data.local.UsuarioSesion
import com.example.teqmed.data.remote.ClienteTarifaCneHttp
import com.example.teqmed.data.repository.TarifaCombustibleRepository
import com.example.teqmed.domain.model.CodigoRol
import com.example.teqmed.domain.model.PropiedadVehiculo
import com.example.teqmed.domain.model.SentidoTraslado
import com.example.teqmed.ui.admin.AdminHomeScreen
import com.example.teqmed.ui.admin.DestinosAdminViewModel
import com.example.teqmed.ui.admin.UsuariosAdminViewModel
import com.example.teqmed.ui.admin.VehiculosAdminViewModel
import com.example.teqmed.ui.auth.AuthViewModel
import com.example.teqmed.ui.auth.ConsentimientoGpsDialog
import com.example.teqmed.ui.auth.LoginScreen
import com.example.teqmed.ui.gerencia.GerenciaHomeScreen
import com.example.teqmed.ui.supervisor.JefeHomeScreen
import com.example.teqmed.ui.supervisor.JefeHomeViewModel
import com.example.teqmed.ui.theme.TEQMEDTheme
import com.example.teqmed.ui.trip.CierreTrasladoScreen
import com.example.teqmed.ui.trip.CierreTrasladoViewModel
import com.example.teqmed.ui.trip.InicioTrasladoScreen
import com.example.teqmed.ui.trip.InicioTrasladoViewModel
import com.example.teqmed.ui.trip.TecnicoHomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = TeqRutaDatabase.getDatabase(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            DatosIniciales.sembrar(db)
        }
        val sessionManager = SessionManager(applicationContext)
        val tarifaRepo = TarifaCombustibleRepository(ClienteTarifaCneHttp(), db.parametroSistemaDao())
        val networkMonitor = com.example.teqmed.util.LiveNetworkMonitor(applicationContext)
        val sincronizador = com.example.teqmed.data.sync.SincronizadorTraslados(
            trasladoDao = db.trasladoDao(),
            networkMonitor = networkMonitor,
            coroutineScope = lifecycleScope
        )

        setContent {
            TEQMEDTheme {
                TeqRutaApp(
                    db = db,
                    sessionManager = sessionManager,
                    tarifaRepo = tarifaRepo
                )
            }
        }
    }
}

/**
 * Orquestador principal de navegación e interfaces de usuario (HU01, HU02, HU03, HU04).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeqRutaApp(
    db: TeqRutaDatabase,
    sessionManager: SessionManager,
    tarifaRepo: TarifaCombustibleRepository
) {
    val sesionActiva by sessionManager.sesionActiva.collectAsState()

    // Control de pantallas internas para el Técnico
    var pantallaTecnicoActual by remember { mutableStateOf<PantallaTecnico>(PantallaTecnico.HOME) }
    var trasladoSeleccionadoParaCierre by remember { mutableStateOf<TrasladoEntity?>(null) }
    var mostrarDialogoConsentimiento by remember { mutableStateOf(false) }

    if (sesionActiva == null) {
        val authViewModel = remember {
            AuthViewModel(
                usuarioDao = db.usuarioDao(),
                rolDao = db.rolDao(),
                sessionManager = sessionManager
            )
        }
        LoginScreen(
            viewModel = authViewModel,
            onLoginExitoso = { sesion ->
                if (sesion.rol == CodigoRol.TECNICO && !sesion.consentimientoGps) {
                    mostrarDialogoConsentimiento = true
                }
            }
        )
    } else {
        val sesion = sesionActiva!!

        // Diálogo informativo de ubicación (HU08)
        if (mostrarDialogoConsentimiento && !sesion.consentimientoGps) {
            ConsentimientoGpsDialog(
                onAceptar = {
                    sessionManager.actualizarConsentimientoGps(true)
                    mostrarDialogoConsentimiento = false
                },
                onRechazar = {
                    mostrarDialogoConsentimiento = false
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "TeqRuta",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${sesion.nombre} · ${sesion.rol.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        if (pantallaTecnicoActual != PantallaTecnico.HOME && sesion.rol == CodigoRol.TECNICO) {
                            IconButton(onClick = { pantallaTecnicoActual = PantallaTecnico.HOME }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            sessionManager.cerrarSesion()
                            pantallaTecnicoActual = PantallaTecnico.HOME
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (sesion.rol) {
                    CodigoRol.TECNICO -> {
                        val destinosFlow = remember { db.destinoDao().obtenerTodos() }
                        val destinosList by destinosFlow.collectAsState(initial = emptyList())
                        val destinosMap = remember(destinosList) { destinosList.associateBy { it.id } }

                        when (pantallaTecnicoActual) {
                            PantallaTecnico.HOME -> {
                                TecnicoHomeScreen(
                                    tecnicoNombre = sesion.nombre,
                                    trasladosFlow = remember { db.trasladoDao().obtenerPorTecnico(sesion.id) },
                                    destinosMap = destinosMap,
                                    onIniciarNuevoTraslado = {
                                        pantallaTecnicoActual = PantallaTecnico.INICIO_TRASLADO
                                    },
                                    onFinalizarTraslado = { traslado ->
                                        trasladoSeleccionadoParaCierre = traslado
                                        pantallaTecnicoActual = PantallaTecnico.CIERRE_TRASLADO
                                    }
                                )
                            }
                            PantallaTecnico.INICIO_TRASLADO -> {
                                val inicioViewModel = remember {
                                    InicioTrasladoViewModel(
                                        destinoDao = db.destinoDao(),
                                        vehiculoDao = db.vehiculoDao(),
                                        trasladoDao = db.trasladoDao(),
                                        tecnicoId = sesion.id
                                    )
                                }
                                InicioTrasladoScreen(
                                    viewModel = inicioViewModel,
                                    onTrasladoIniciado = { _ ->
                                        pantallaTecnicoActual = PantallaTecnico.HOME
                                    }
                                )
                            }
                            PantallaTecnico.CIERRE_TRASLADO -> {
                                val traslado = trasladoSeleccionadoParaCierre
                                if (traslado != null) {
                                    val destino = destinosMap[traslado.destinoId]
                                    var vehiculoEntity by remember { mutableStateOf<com.example.teqmed.data.local.VehiculoEntity?>(null) }

                                    androidx.compose.runtime.LaunchedEffect(traslado.vehiculoPatente) {
                                        vehiculoEntity = db.vehiculoDao().obtenerPorPatente(traslado.vehiculoPatente)
                                    }

                                    val propiedad = try {
                                        PropiedadVehiculo.valueOf(vehiculoEntity?.propiedad ?: PropiedadVehiculo.EMPRESA.name)
                                    } catch (_: Exception) {
                                        PropiedadVehiculo.EMPRESA
                                    }

                                    val rendimiento = vehiculoEntity?.rendimientoKmL ?: 12.0
                                    val distOficial = destino?.distanciaOficialKm ?: 50.0
                                    val sentido = try {
                                        SentidoTraslado.valueOf(traslado.sentido)
                                    } catch (_: Exception) {
                                        SentidoTraslado.IDA
                                    }

                                    val cierreViewModel = remember(traslado.id) {
                                        CierreTrasladoViewModel(
                                            trasladoId = traslado.id,
                                            odometroInicial = traslado.odometroInicial,
                                            distanciaOficialKm = distOficial,
                                            propiedadVehiculo = propiedad,
                                            rendimientoKmL = rendimiento,
                                            sentido = sentido,
                                            tarifaRepo = tarifaRepo,
                                            parametroDao = db.parametroSistemaDao(),
                                            trasladoDao = db.trasladoDao(),
                                            peajeDao = db.peajeDao()
                                        )
                                    }

                                    CierreTrasladoScreen(
                                        viewModel = cierreViewModel,
                                        onTrasladoFinalizado = {
                                            pantallaTecnicoActual = PantallaTecnico.HOME
                                            trasladoSeleccionadoParaCierre = null
                                        }
                                    )
                                } else {
                                    pantallaTecnicoActual = PantallaTecnico.HOME
                                }
                            }
                        }
                    }

                    CodigoRol.JEFE -> {
                        val jefeViewModel = remember {
                            JefeHomeViewModel(
                                trasladoDao = db.trasladoDao(),
                                destinoDao = db.destinoDao(),
                                jefeId = sesion.id
                            )
                        }
                        JefeHomeScreen(
                            jefeNombre = sesion.nombre,
                            viewModel = jefeViewModel
                        )
                    }

                    CodigoRol.ADMIN -> {
                        val destinosViewModel = remember { DestinosAdminViewModel(db.destinoDao()) }
                        val vehiculosViewModel = remember { VehiculosAdminViewModel(db.vehiculoDao()) }
                        val usuariosViewModel = remember { UsuariosAdminViewModel(db.usuarioDao(), db.rolDao()) }
                        val parametrosViewModel = remember {
                            com.example.teqmed.ui.admin.ParametrosAdminViewModel(
                                parametroDao = db.parametroSistemaDao(),
                                tarifaRepo = tarifaRepo
                            )
                        }

                        AdminHomeScreen(
                            destinosViewModel = destinosViewModel,
                            vehiculosViewModel = vehiculosViewModel,
                            usuariosViewModel = usuariosViewModel,
                            parametrosViewModel = parametrosViewModel
                        )
                    }

                    CodigoRol.GERENCIA -> {
                        val destinosFlow = remember { db.destinoDao().obtenerTodos() }
                        val destinosList by destinosFlow.collectAsState(initial = emptyList())
                        val destinosMap = remember(destinosList) { destinosList.associateBy { it.id } }

                        GerenciaHomeScreen(
                            gerenteNombre = sesion.nombre,
                            trasladosFlow = remember { db.trasladoDao().obtenerTodos() },
                            destinosMap = destinosMap
                        )
                    }
                }
            }
        }
    }
}

enum class PantallaTecnico {
    HOME,
    INICIO_TRASLADO,
    CIERRE_TRASLADO
}
