package com.kaquenduri.prueba01_mqtt.Presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaquenduri.prueba01_mqtt.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenarModulosScreen(
    irHome: () -> Unit
) {
    // Estados para el orden de los módulos
    var modulos by remember { mutableStateOf(listOf(
        ModuloPantalla("Sensores", "Muestra datos de humedad, temperatura y humedad del aire", true, 0),
        ModuloPantalla("Gráficos", "Visualización de datos en tiempo real", true, 1),
        ModuloPantalla("Alertas", "Notificaciones y configuración de alertas", true, 2),
        ModuloPantalla("Estadísticas", "Resumen y análisis de datos", true, 3),
        ModuloPantalla("Cultivos", "Gestión de cultivos y plantas", true, 4),
        ModuloPantalla("Configuración", "Ajustes generales de la aplicación", true, 5),
        ModuloPantalla("Chat IA", "Asistente inteligente para recomendaciones", false, 6),
        ModuloPantalla("Historial", "Registro histórico de mediciones", false, 7)
    ))}
    
    Scaffold(
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instrucciones
            item {
                InstruccionesCard()
            }
            
            // Módulos Activos
            item {
                ModulosActivosCard(
                    modulos = modulos.filter { it.activo },
                    onModuloToggle = { modulo ->
                        modulos = modulos.map { 
                            if (it.nombre == modulo.nombre) it.copy(activo = !it.activo) else it 
                        }
                    },
                    onModuloMoveUp = { modulo ->
                        val index = modulos.indexOf(modulo)
                        if (index > 0) {
                            modulos = modulos.toMutableList().apply {
                                removeAt(index)
                                add(index - 1, modulo)
                            }
                        }
                    },
                    onModuloMoveDown = { modulo ->
                        val index = modulos.indexOf(modulo)
                        if (index < modulos.size - 1) {
                            modulos = modulos.toMutableList().apply {
                                removeAt(index)
                                add(index + 1, modulo)
                            }
                        }
                    }
                )
            }
            
            // Módulos Inactivos
            item {
                ModulosInactivosCard(
                    modulos = modulos.filter { !it.activo },
                    onModuloToggle = { modulo ->
                        modulos = modulos.map { 
                            if (it.nombre == modulo.nombre) it.copy(activo = !it.activo) else it 
                        }
                    }
                )
            }
            
            // Configuraciones Predefinidas
            item {
                ConfiguracionesPredefinidasCard(
                    onAplicarConfiguracion = { configuracion ->
                        modulos = configuracion.mapIndexed { index, nombre ->
                            val moduloOriginal = modulos.find { it.nombre == nombre } ?: ModuloPantalla(nombre, "", false, index)
                            moduloOriginal.copy(activo = true, orden = index)
                        }
                    }
                )
            }
            
            // Vista Previa
            item {
                VistaPreviaModulosCard(modulos = modulos.filter { it.activo })
            }
        }
    }
}

@Composable
fun InstruccionesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Instrucciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            val instrucciones = listOf(
                "• Activa/desactiva módulos según tus necesidades",
                "• Usa las flechas para cambiar el orden de aparición",
                "• Los módulos activos aparecerán en la pantalla principal",
                "• Puedes aplicar configuraciones predefinidas"
            )
            
            instrucciones.forEach { instruccion ->
                Text(
                    text = instruccion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun ModulosActivosCard(
    modulos: List<ModuloPantalla>,
    onModuloToggle: (ModuloPantalla) -> Unit,
    onModuloMoveUp: (ModuloPantalla) -> Unit,
    onModuloMoveDown: (ModuloPantalla) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Módulos Activos (${modulos.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            if (modulos.isEmpty()) {
                Text(
                    text = "No hay módulos activos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                modulos.sortedBy { it.orden }.forEach { modulo ->
                    ModuloItem(
                        modulo = modulo,
                        onToggle = { onModuloToggle(modulo) },
                        onMoveUp = { onModuloMoveUp(modulo) },
                        onMoveDown = { onModuloMoveDown(modulo) },
                        showControls = true
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ModulosInactivosCard(
    modulos: List<ModuloPantalla>,
    onModuloToggle: (ModuloPantalla) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Módulos Inactivos (${modulos.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            if (modulos.isEmpty()) {
                Text(
                    text = "Todos los módulos están activos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                modulos.forEach { modulo ->
                    ModuloItem(
                        modulo = modulo,
                        onToggle = { onModuloToggle(modulo) },
                        onMoveUp = { },
                        onMoveDown = { },
                        showControls = false
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ModuloItem(
    modulo: ModuloPantalla,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    showControls: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (modulo.activo) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modulo.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = modulo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showControls) {
                    IconButton(onClick = onMoveUp) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Mover arriba"
                        )
                    }
                    IconButton(onClick = onMoveDown) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Mover abajo"
                        )
                    }
                }
                
                Switch(
                    checked = modulo.activo,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }
}

@Composable
fun ConfiguracionesPredefinidasCard(
    onAplicarConfiguracion: (List<String>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Configuraciones Predefinidas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            val configuraciones = mapOf(
                "Básica" to listOf("Sensores", "Alertas", "Cultivos"),
                "Completa" to listOf("Sensores", "Gráficos", "Alertas", "Estadísticas", "Cultivos", "Configuración"),
                "Avanzada" to listOf("Sensores", "Gráficos", "Alertas", "Estadísticas", "Cultivos", "Configuración", "Chat IA", "Historial"),
                "Minimalista" to listOf("Sensores", "Alertas")
            )
            
            configuraciones.forEach { (nombre, modulos) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${modulos.size} módulos: ${modulos.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = { onAplicarConfiguracion(modulos) }) {
                        Text("Aplicar")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun VistaPreviaModulosCard(modulos: List<ModuloPantalla>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vista Previa del Orden",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            if (modulos.isEmpty()) {
                Text(
                    text = "No hay módulos activos para mostrar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                modulos.sortedBy { it.orden }.forEachIndexed { index, modulo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = modulo.nombre,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

data class ModuloPantalla(
    val nombre: String,
    val descripcion: String,
    val activo: Boolean,
    val orden: Int
)
