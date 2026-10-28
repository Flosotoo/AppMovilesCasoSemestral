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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Pantalla CRUD del Administrador para Cuentas de Usuario y Roles (HU05, HU06, RF-34, RF-35).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosAdminScreen(
    viewModel: UsuariosAdminViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var expandedRol by remember { mutableStateOf(false) }

    val rolesMap = state.roles.associateBy { it.id }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::abrirDialogoCrear,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Usuario") }
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
                    text = "Control de Usuarios y Roles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Administración de cuentas, roles y permisos de acceso",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(state.usuarios) { usuario ->
                val rol = rolesMap[usuario.rolId]
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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (usuario.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Column {
                                Text(
                                    text = usuario.nombre,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${usuario.rut} · ${usuario.email}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Rol: ${rol?.nombre ?: "ID #${usuario.rolId}"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Switch de baja lógica (HU06 / RF-35)
                        Switch(
                            checked = usuario.activo,
                            onCheckedChange = { nuevoEstado ->
                                viewModel.cambiarEstadoActivo(usuario.id, nuevoEstado)
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
        val rolSeleccionado = rolesMap[form.rolId]

        AlertDialog(
            onDismissRequest = viewModel::cerrarDialogoCrear,
            title = { Text("Registrar Nuevo Usuario", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = form.rut,
                        onValueChange = viewModel::onFormRutChange,
                        label = { Text("RUT (con guión)") },
                        placeholder = { Text("12345678-9") },
                        isError = form.rutError != null,
                        supportingText = { form.rutError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.nombre,
                        onValueChange = viewModel::onFormNombreChange,
                        label = { Text("Nombre Completo") },
                        placeholder = { Text("Juan Pérez") },
                        isError = form.nombreError != null,
                        supportingText = { form.nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.email,
                        onValueChange = viewModel::onFormEmailChange,
                        label = { Text("Correo Electrónico") },
                        placeholder = { Text("juan.perez@teqmed.cl") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = form.emailError != null,
                        supportingText = { form.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.clave,
                        onValueChange = viewModel::onFormClaveChange,
                        label = { Text("Contraseña Inicial") },
                        isError = form.claveError != null,
                        supportingText = { form.claveError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dropdown de Roles RBAC
                    ExposedDropdownMenuBox(
                        expanded = expandedRol,
                        onExpandedChange = { expandedRol = !expandedRol }
                    ) {
                        OutlinedTextField(
                            value = rolSeleccionado?.nombre ?: "Seleccione Rol",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rol Asignado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRol,
                            onDismissRequest = { expandedRol = false }
                        ) {
                            state.roles.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r.nombre) },
                                    onClick = {
                                        viewModel.onFormRolChange(r.id)
                                        expandedRol = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = viewModel::guardarNuevoUsuario) {
                    Text("Crear Usuario")
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
