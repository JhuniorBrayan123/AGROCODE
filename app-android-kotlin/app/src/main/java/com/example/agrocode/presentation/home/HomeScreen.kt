package com.example.agrocode.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrocode.domain.model.TipoSensor
import com.example.agrocode.presentation.notifications.Notifier
import com.example.agrocode.presentation.theme.AGROCODETheme
import com.example.agrocode.presentation.theme.GreenPrimary
import com.example.agrocode.presentation.theme.WhiteBackground

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val estadoUI by viewModel.estadoUI.collectAsState()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var confirmarToggleId by remember { mutableStateOf<String?>(null) }
    var confirmarActivar by remember { mutableStateOf(true) }
    var intervaloEditable by remember { mutableStateOf(estadoUI.intervaloMs.toString()) }
    LaunchedEffect(estadoUI.intervaloMs) { intervaloEditable = estadoUI.intervaloMs.toString() }
    // Notificar nuevas alertas críticas simples
    var ultimaAlertaSize by remember { mutableStateOf(0) }
    LaunchedEffect(estadoUI.alertasCriticas.size) {
        if (estadoUI.alertasCriticas.size > ultimaAlertaSize) {
            val nueva = estadoUI.alertasCriticas.lastOrNull()
            if (nueva != null) Notifier.notifyCritical(ctx, "Alerta crítica", nueva.mensaje)
            ultimaAlertaSize = estadoUI.alertasCriticas.size
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GreenPrimary.copy(alpha = 0.05f),
                            WhiteBackground
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🌱 AGROCODE",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Monitoreo de Cultivos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado actual del vivero",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    // Control intervalo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Intervalo (ms): ", style = MaterialTheme.typography.bodySmall)
                        androidx.compose.material3.OutlinedTextField(
                            value = intervaloEditable,
                            onValueChange = { intervaloEditable = it.filter { ch -> ch.isDigit() } },
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(44.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { intervaloEditable.toLongOrNull()?.let { viewModel.cambiarIntervalo(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) { Text("Aplicar", color = Color.White) }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Selección de sensores y métricas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val promHum = estadoUI.promedioDiario.entries.firstOrNull { (id, _) ->
                        estadoUI.sensores.firstOrNull { it.id == id }?.tipo == TipoSensor.HUMEDAD
                    }?.value
                    val promTmp = estadoUI.promedioDiario.entries.firstOrNull { (id, _) ->
                        estadoUI.sensores.firstOrNull { it.id == id }?.tipo == TipoSensor.TEMPERATURA
                    }?.value
                    TarjetaMetrica(
                        titulo = "Humedad (promedio)",
                        valor = if (promHum != null) "${promHum.toInt()}%" else "--%",
                        icono = Icons.Default.WaterDrop,
                        colorIcono = Color(0xFF2196F3),
                        modificador = Modifier.weight(1f)
                    )
                    TarjetaMetrica(
                        titulo = "Temperatura (promedio)",
                        valor = if (promTmp != null) "${"%.1f".format(promTmp)}°C" else "--°C",
                        icono = Icons.Default.Thermostat,
                        colorIcono = Color(0xFFFF9800),
                        modificador = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Card de estado general y selección/activación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Estado del Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Conexión ESP32:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Sensores",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            estadoUI.sensores.forEach { sensor ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "${sensor.nombre} (${if (sensor.activo) "Activo" else "Inactivo"})")
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = {
                                                confirmarToggleId = sensor.id
                                                confirmarActivar = !sensor.activo
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (sensor.activo) Color(0xFFF44336) else Color(0xFF4CAF50)
                                            )
                                        ) {
                                            Text(text = if (sensor.activo) "Desactivar" else "Activar", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Historial simple y alertas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteBackground)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Alertas críticas", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        estadoUI.alertasCriticas.takeLast(5).forEach { a ->
                            Text("• ${a.mensaje}")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Historial de acciones", fontWeight = FontWeight.Bold)
                        estadoUI.historialAcciones.takeLast(5).forEach { acc ->
                            Text("• ${acc.tipo} - ${acc.detalle}")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Historial por sensor", fontWeight = FontWeight.Bold)
                        estadoUI.lecturasRecientes.entries.take(3).forEach { (id, lista) ->
                            val nombre = estadoUI.sensores.firstOrNull { it.id == id }?.nombre ?: id
                            val ultimo = lista.lastOrNull()
                            Text("• $nombre: " + when {
                                ultimo?.humedadPorcentaje != null -> "${ultimo.humedadPorcentaje}%"
                                ultimo?.temperaturaCelsius != null -> "${ultimo.temperaturaCelsius}°C"
                                else -> "--"
                            })
                        }
                    }
                }
            }
        }
    }

    if (confirmarToggleId != null) {
        AlertDialog(
            onDismissRequest = { confirmarToggleId = null },
            confirmButton = {
                Button(onClick = {
                    viewModel.toggleSensor(confirmarToggleId!!, confirmarActivar)
                    confirmarToggleId = null
                }, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) { Text("Confirmar", color = Color.White) }
            },
            dismissButton = {
                Button(onClick = { confirmarToggleId = null }) { Text("Cancelar") }
            },
            title = { Text(if (confirmarActivar) "Activar sensor" else "Desactivar sensor") },
            text = { Text("¿Deseas ${if (confirmarActivar) "activar" else "desactivar"} este sensor?") }
        )
    }
}

@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorIcono: Color,
    modificador: Modifier = Modifier
) {
    Card(
        modifier = modificador,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBackground)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AGROCODETheme {
        HomeScreen()
    }
}
