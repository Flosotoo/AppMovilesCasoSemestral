package com.example.teqmed.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Pantalla principal del Administrador del Sistema con pestañas maestras (HU05, HU06, HU19, HU34, HU35, HU36).
 */
@Composable
fun AdminHomeScreen(
    destinosViewModel: DestinosAdminViewModel,
    vehiculosViewModel: VehiculosAdminViewModel,
    usuariosViewModel: UsuariosAdminViewModel,
    parametrosViewModel: ParametrosAdminViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Destinos", "Vehículos", "Usuarios", "Tarifas")

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.LocalHospital, contentDescription = null)
                                1 -> Icon(Icons.Default.DirectionsCar, contentDescription = null)
                                2 -> Icon(Icons.Default.People, contentDescription = null)
                                else -> Icon(Icons.Default.Tune, contentDescription = null)
                            }
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> DestinosAdminScreen(destinosViewModel)
                1 -> VehiculosAdminScreen(vehiculosViewModel)
                2 -> UsuariosAdminScreen(usuariosViewModel)
                3 -> ParametrosAdminScreen(parametrosViewModel)
            }
        }
    }
}
