package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaquenduri.prueba01_mqtt.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesPushScreen(
    irHome: () -> Unit
) {
    // Estados para configuración de notificaciones
    var notificacionesActivas by remember { mutableStateOf(true) }
    var notificacionesHumedad by remember { mutableStateOf(true) }
    var notificacionesTemperatura by remember { mutableStateOf(true) }
    var notificacionesSistema by remember { mutableStateOf(true) }
    var frecuenciaNotificaciones by remember { mutableStateOf("Inmediata") }
    var horarioNotificaciones by remember { mutableStateOf("24/7") }
    
    val frecuencias = listOf("Inmediata", "Cada 5 min", "Cada 15 min", "Cada hora")
    val horarios = listOf("24/7", "Solo día (6AM-10PM)", "Solo noche (10PM-6AM)", "Personalizado")
    
    Scaffold(
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado General
            item {
                EstadoNotificacionesCard(
                    notificacionesActivas = notificacionesActivas,
                    onNotificacionesChange = { notificacionesActivas = it }
                )
            }
            
            // Tipos de Notificaciones
            item {
                TiposNotificacionesCard(
                    notificacionesHumedad = notificacionesHumedad,
                    onHumedadChange = { notificacionesHumedad = it },
                    notificacionesTemperatura = notificacionesTemperatura,
                    onTemperaturaChange = { notificacionesTemperatura = it },
                    notificacionesSistema = notificacionesSistema,
                    onSistemaChange = { notificacionesSistema = it }
                )
            }
            
            // Configuración de Frecuencia
            item {
                ConfiguracionFrecuenciaCard(
                    frecuencia = frecuenciaNotificaciones,
                    frecuenciasDisponibles = frecuencias,
                    onFrecuenciaChange = { frecuenciaNotificaciones = it }
                )
            }
            
            // Configuración de Horario
            item {
                ConfiguracionHorarioCard(
                    horario = horarioNotificaciones,
                    horariosDisponibles = horarios,
                    onHorarioChange = { horarioNotificaciones = it }
                )
            }
            
            // Vista Previa
            item {
                VistaPreviaNotificacionesCard(
                    activas = notificacionesActivas,
                    tipos = listOf(
                        "Humedad" to notificacionesHumedad,
                        "Temperatura" to notificacionesTemperatura,
                        "Sistema" to notificacionesSistema
                    ),
                    frecuencia = frecuenciaNotificaciones,
                    horario = horarioNotificaciones
                )
            }
            
            // Historial de Notificaciones
            item {
                HistorialNotificacionesCard()
            }
        }
    }
}

@Composable
fun EstadoNotificacionesCard(
    notificacionesActivas: Boolean,
    onNotificacionesChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estado de Notificaciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (notificacionesActivas) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = if (notificacionesActivas) Color(0xFF2E7D32) else Color(0xFF757575)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Notificaciones Push")
                }
                Switch(
                    checked = notificacionesActivas,
                    onCheckedChange = onNotificacionesChange
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = if (notificacionesActivas) 
                    "✅ Las notificaciones están activas y funcionando" 
                else 
                    "❌ Las notificaciones están desactivadas",
                style = MaterialTheme.typography.bodyMedium,
                color = if (notificacionesActivas) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun TiposNotificacionesCard(
    notificacionesHumedad: Boolean,
    onHumedadChange: (Boolean) -> Unit,
    notificacionesTemperatura: Boolean,
    onTemperaturaChange: (Boolean) -> Unit,
    notificacionesSistema: Boolean,
    onSistemaChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tipos de Notificaciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            
            // Notificaciones de Humedad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Humedad del Suelo", fontWeight = FontWeight.Medium)
                    Text(
                        text = "Alertas por niveles críticos de humedad",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificacionesHumedad,
                    onCheckedChange = onHumedadChange
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Notificaciones de Temperatura
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Temperatura", fontWeight = FontWeight.Medium)
                    Text(
                        text = "Alertas por temperaturas extremas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificacionesTemperatura,
                    onCheckedChange = onTemperaturaChange
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Notificaciones del Sistema
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sistema", fontWeight = FontWeight.Medium)
                    Text(
                        text = "Alertas de conexión y estado del sistema",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificacionesSistema,
                    onCheckedChange = onSistemaChange
                )
            }
        }
    }
}

@Composable
fun ConfiguracionFrecuenciaCard(
    frecuencia: String,
    frecuenciasDisponibles: List<String>,
    onFrecuenciaChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Frecuencia de Notificaciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Box {
                OutlinedTextField(
                    value = frecuencia,
                    onValueChange = {},
                    label = { Text("Seleccionar frecuencia") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    frecuenciasDisponibles.forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq) },
                            onClick = {
                                onFrecuenciaChange(freq)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = when (frecuencia) {
                    "Inmediata" -> "Recibirás notificaciones tan pronto como se detecte un problema"
                    "Cada 5 min" -> "Las notificaciones se agruparán cada 5 minutos"
                    "Cada 15 min" -> "Las notificaciones se agruparán cada 15 minutos"
                    "Cada hora" -> "Recibirás un resumen cada hora"
                    else -> "Configuración personalizada"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ConfiguracionHorarioCard(
    horario: String,
    horariosDisponibles: List<String>,
    onHorarioChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Horario de Notificaciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Box {
                OutlinedTextField(
                    value = horario,
                    onValueChange = {},
                    label = { Text("Seleccionar horario") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    horariosDisponibles.forEach { hor ->
                        DropdownMenuItem(
                            text = { Text(hor) },
                            onClick = {
                                onHorarioChange(hor)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = when (horario) {
                    "24/7" -> "Recibirás notificaciones en cualquier momento"
                    "Solo día (6AM-10PM)" -> "Notificaciones solo durante el día"
                    "Solo noche (10PM-6AM)" -> "Notificaciones solo durante la noche"
                    "Personalizado" -> "Configura horarios específicos"
                    else -> "Horario personalizado"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VistaPreviaNotificacionesCard(
    activas: Boolean,
    tipos: List<Pair<String, Boolean>>,
    frecuencia: String,
    horario: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vista Previa de Configuración",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estado:")
                Text(
                    text = if (activas) "Activo" else "Inactivo",
                    color = if (activas) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Frecuencia:")
                Text(frecuencia, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Horario:")
                Text(horario, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text("Tipos activos:", fontWeight = FontWeight.Medium)
            tipos.filter { it.second }.forEach { (tipo, _) ->
                Text(
                    text = "• $tipo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (tipos.none { it.second }) {
                Text(
                    text = "• Ningún tipo activo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HistorialNotificacionesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Historial de Notificaciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            // Simulación de historial
            val historial = listOf(
                "Hace 2 horas: Humedad baja detectada (15%)",
                "Hace 5 horas: Temperatura alta (35°C)",
                "Ayer: Sistema reconectado exitosamente",
                "Ayer: Humedad crítica (5%)"
            )
            
            if (historial.isEmpty()) {
                Text(
                    text = "No hay notificaciones recientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                historial.forEach { notificacion ->
                    Text(
                        text = "• $notificacion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = { /* Limpiar historial */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpiar Historial")
            }
        }
    }
}
