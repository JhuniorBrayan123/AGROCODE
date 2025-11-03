package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaquenduri.prueba01_mqtt.R
import com.kaquenduri.prueba01_mqtt.ViewModels.ConfiguracionVisualViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionVisualScreen(
    viewModel: ConfiguracionVisualViewModel,
    irHome: () -> Unit
) {
    val colorPrimarioSeleccionado by viewModel.colorPrimarioSeleccionado
    val colorSecundarioSeleccionado by viewModel.colorSecundarioSeleccionado
    val tamañoTextoGlobal by viewModel.tamañoTextoGlobal
    
    val colorPrimarioActual = viewModel.getColorPrimarioActual()
    val colorSecundarioActual = viewModel.getColorSecundarioActual()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_plant),
                            contentDescription = "agrocode logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Configuración • agrocode",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { irHome() }) {
                        Icon(Icons.Default.Home, contentDescription = "Ir a Home", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2E7D32)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF5F5F5)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { irHome() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección de Colores
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Colores",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        // Color Primario
                        Text("Color Primario")
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Verde original
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        color = Color(0xFF2E7D32),
                                        shape = CircleShape
                                    )
                                    .selectable(
                                        selected = colorPrimarioSeleccionado == 0,
                                        onClick = { viewModel.actualizarColorPrimario(0) }
                                    )
                            )
                            
                            // Cyan oscuro
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        color = Color(0xFF00695C),
                                        shape = CircleShape
                                    )
                                    .selectable(
                                        selected = colorPrimarioSeleccionado == 1,
                                        onClick = { viewModel.actualizarColorPrimario(1) }
                                    )
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Color Secundario
                        Text("Color Secundario")
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Naranja original
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        color = Color(0xFFFFB278),
                                        shape = CircleShape
                                    )
                                    .selectable(
                                        selected = colorSecundarioSeleccionado == 0,
                                        onClick = { viewModel.actualizarColorSecundario(0) }
                                    )
                            )
                            
                            // Amarillo verdoso
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        color = Color(0xFF8BC34A),
                                        shape = CircleShape
                                    )
                                    .selectable(
                                        selected = colorSecundarioSeleccionado == 1,
                                        onClick = { viewModel.actualizarColorSecundario(1) }
                                    )
                            )
                        }
                    }
                }

                // Sección de Tamaño de Texto
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tamaño de Texto",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        Text("Tamaño Global: ${tamañoTextoGlobal.toInt()}sp")
                        Slider(
                            value = tamañoTextoGlobal,
                            onValueChange = { viewModel.actualizarTamañoTexto(it) },
                            valueRange = 12f..24f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            text = "Este cambio afectará todo el texto de la aplicación",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }


                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            viewModel.resetearConfiguracion()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reiniciar")
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.guardarConfiguracion()
                            irHome()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}
