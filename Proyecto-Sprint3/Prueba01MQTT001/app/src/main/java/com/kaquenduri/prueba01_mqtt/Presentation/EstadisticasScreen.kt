package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.kaquenduri.prueba01_mqtt.R
import com.kaquenduri.prueba01_mqtt.ViewModels.SensorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: SensorViewModel,
    irHome: () -> Unit
) {
    val mensajeH by viewModel.mensajeHumedad
    val mensajeT by viewModel.mensajeTemperatura
    val mensajeHA by viewModel.mensajeHumedadAire
    val alertas by viewModel.alertas
    val conectado by viewModel.conectado
    
    // Datos actuales
    val humedadActual = remember(mensajeH) {
        Regex("""\d+""").find(mensajeH)?.value?.toFloatOrNull() ?: 0f
    }
    val temperaturaActual = remember(mensajeT) {
        Regex("""\d+""").find(mensajeT)?.value?.toFloatOrNull() ?: 0f
    }
    val humedadAireActual = remember(mensajeHA) {
        Regex("""\d+""").find(mensajeHA)?.value?.toFloatOrNull() ?: 0f
    }
    
    Scaffold(
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado del Sistema
            item {
                EstadoSistemaCard(
                    conectado = conectado,
                    fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                )
            }
            
            // Resumen de Sensores
            item {
                ResumenSensoresCard(
                    humedad = humedadActual,
                    temperatura = temperaturaActual,
                    humedadAire = humedadAireActual
                )
            }
            
            // Configuración de Alertas
            item {
                ConfiguracionAlertasCard(
                    alertaHumedadAltaActiva = viewModel.alertaHumedadAltaActiva.value,
                    alertaHumedadBajaActiva = viewModel.alertaHumedadBajaActiva.value
                )
            }
            
            // Estadísticas de Alertas
            item {
                EstadisticasAlertasCard(alertas = alertas)
            }
            
            // Recomendaciones del Sistema
            item {
                RecomendacionesCard(
                    humedad = humedadActual,
                    temperatura = temperaturaActual,
                    humedadAire = humedadAireActual
                )
            }
        }
    }
}

@Composable
fun EstadoSistemaCard(
    conectado: Boolean,
    fechaActual: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estado del Sistema",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Conexión MQTT:")
                Text(
                    text = if (conectado) "Conectado" else "Desconectado",
                    color = if (conectado) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Última actualización:")
                Text(
                    text = fechaActual,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ResumenSensoresCard(
    humedad: Float,
    temperatura: Float,
    humedadAire: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen de Sensores",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SensorResumenItem("Humedad Suelo", humedad, "%", Color(0xFF2E7D32))
                SensorResumenItem("Temperatura", temperatura, "°C", Color(0xFFD32F2F))
                SensorResumenItem("Humedad Aire", humedadAire, "%", Color(0xFF1976D2))
            }
        }
    }
}

@Composable
fun SensorResumenItem(
    nombre: String,
    valor: Float,
    unidad: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = nombre,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${valor.roundToInt()}$unidad",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ConfiguracionAlertasCard(
    alertaHumedadAltaActiva: Boolean,
    alertaHumedadBajaActiva: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Configuración de Alertas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Alerta Humedad Alta:")
                Text(
                    text = if (alertaHumedadAltaActiva) "Activa" else "Inactiva",
                    color = if (alertaHumedadAltaActiva) Color(0xFF2E7D32) else Color(0xFF757575),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Alerta Humedad Baja:")
                Text(
                    text = if (alertaHumedadBajaActiva) "Activa" else "Inactiva",
                    color = if (alertaHumedadBajaActiva) Color(0xFF2E7D32) else Color(0xFF757575),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EstadisticasAlertasCard(alertas: List<com.kaquenduri.prueba01_mqtt.models.entities.Alerta>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estadísticas de Alertas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total de alertas:")
                Text(
                    text = "${alertas.size}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Alertas hoy:")
                Text(
                    text = "${alertas.count { 
                        val hoy = Calendar.getInstance()
                        val fechaAlerta = Calendar.getInstance().apply { timeInMillis = it.fechaHora }
                        hoy.get(Calendar.DAY_OF_YEAR) == fechaAlerta.get(Calendar.DAY_OF_YEAR) &&
                        hoy.get(Calendar.YEAR) == fechaAlerta.get(Calendar.YEAR)
                    }}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun RecomendacionesCard(
    humedad: Float,
    temperatura: Float,
    humedadAire: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recomendaciones del Sistema",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            val recomendaciones = generarRecomendaciones(humedad, temperatura, humedadAire)
            
            recomendaciones.forEach { recomendacion ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = recomendacion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

fun generarRecomendaciones(humedad: Float, temperatura: Float, humedadAire: Float): List<String> {
    val recomendaciones = mutableListOf<String>()
    
    when {
        humedad < 30 -> recomendaciones.add("El suelo está muy seco. Considera regar con moderación.")
        humedad > 80 -> recomendaciones.add("Humedad muy alta. Suspende el riego temporalmente.")
        humedad in 30f..60f -> recomendaciones.add("Nivel de humedad óptimo. Mantén el riego actual.")
    }
    
    when {
        temperatura > 30 -> recomendaciones.add("Temperatura alta. Considera sombra o ventilación.")
        temperatura < 15 -> recomendaciones.add("Temperatura baja. Protege las plantas del frío.")
        temperatura in 15f..30f -> recomendaciones.add("Temperatura ideal para el crecimiento.")
    }
    
    when {
        humedadAire < 30 -> recomendaciones.add("Aire muy seco. Considera humidificar el ambiente.")
        humedadAire > 80 -> recomendaciones.add("Aire muy húmedo. Mejora la ventilación.")
        humedadAire in 30f..80f -> recomendaciones.add("Humedad del aire en rango óptimo.")
    }
    
    if (recomendaciones.isEmpty()) {
        recomendaciones.add("Todas las condiciones están dentro de rangos óptimos.")
    }
    
    return recomendaciones
}
