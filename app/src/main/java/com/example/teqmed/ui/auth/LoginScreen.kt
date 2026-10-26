package com.example.teqmed.ui.auth

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.teqmed.data.local.UsuarioSesion

/**
 * Pantalla de inicio de sesión Material 3 (HU01).
 * Regla inquebrantable: Prohibido validar campos dentro del Composable.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (UsuarioSesion) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.usuarioAutenticado) {
        state.usuarioAutenticado?.let { sesion ->
            onLoginExitoso(sesion)
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cabecera Corporativa
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TR",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TeqRuta",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Gestión de Traslados en Terreno — TEQMED SpA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de Formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Email
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Correo Electrónico") },
                        placeholder = { Text("ejemplo@teqmed.cl") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email")
                        },
                        isError = state.emailError != null,
                        supportingText = {
                            state.emailError?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Contraseña
                    OutlinedTextField(
                        value = state.clave,
                        onValueChange = viewModel::onClaveChange,
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Contraseña")
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar clave" else "Mostrar clave"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = state.claveError != null,
                        supportingText = {
                            state.claveError?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mensaje de Error General con Animación
                    AnimatedVisibility(
                        visible = state.errorGeneral != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.errorGeneral?.let { mensaje ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = mensaje,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de Ingreso
                    Button(
                        onClick = viewModel::iniciarSesion,
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
                            Text("Acceder al Sistema")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Atajos de prueba rápida para Live Coding / Defensa (Parcial 3)
            Text(
                text = "Acceso Rápido para Pruebas (Semillas):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = state.email == "tecnico@teqmed.cl",
                    onClick = {
                        viewModel.onEmailChange("tecnico@teqmed.cl")
                        viewModel.onClaveChange("tecnico123")
                    },
                    label = { Text("Técnico") }
                )
                FilterChip(
                    selected = state.email == "jefe@teqmed.cl",
                    onClick = {
                        viewModel.onEmailChange("jefe@teqmed.cl")
                        viewModel.onClaveChange("jefe123")
                    },
                    label = { Text("Jefe") }
                )
                FilterChip(
                    selected = state.email == "gerencia@teqmed.cl",
                    onClick = {
                        viewModel.onEmailChange("gerencia@teqmed.cl")
                        viewModel.onClaveChange("gerencia123")
                    },
                    label = { Text("Gerencia") }
                )
                FilterChip(
                    selected = state.email == "admin@teqmed.cl",
                    onClick = {
                        viewModel.onEmailChange("admin@teqmed.cl")
                        viewModel.onClaveChange("admin123")
                    },
                    label = { Text("Admin") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            var mostrarAvisoGps by remember { mutableStateOf(false) }

            TextButton(onClick = { mostrarAvisoGps = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aviso de Auditoría GPS y Privacidad (RF-38)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (mostrarAvisoGps) {
                ConsentimientoGpsDialog(
                    onAceptar = { mostrarAvisoGps = false },
                    onRechazar = { mostrarAvisoGps = false }
                )
            }
        }
    }
}
