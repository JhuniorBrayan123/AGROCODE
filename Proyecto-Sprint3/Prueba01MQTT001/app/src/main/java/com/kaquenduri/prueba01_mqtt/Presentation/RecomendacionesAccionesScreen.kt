package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import kotlin.math.roundToInt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaquenduri.prueba01_mqtt.R
import com.kaquenduri.prueba01_mqtt.ViewModels.SensorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecomendacionesAccionesScreen(
    viewModel: SensorViewModel,
    irHome: () -> Unit
) {
    val mensajeH by viewModel.mensajeHumedad
    val mensajeT by viewModel.mensajeTemperatura
    val mensajeHA by viewModel.mensajeHumedadAire
    
    // Datos actuales de sensores
    val humedadActual = remember(mensajeH) {
        Regex("""\d+""").find(mensajeH)?.value?.toFloatOrNull() ?: 0f
    }
    val temperaturaActual = remember(mensajeT) {
        Regex("""\d+""").find(mensajeT)?.value?.toFloatOrNull() ?: 0f
    }
    val humedadAireActual = remember(mensajeHA) {
        Regex("""\d+""").find(mensajeHA)?.value?.toFloatOrNull() ?: 0f
    }
    
    // Generar recomendaciones basadas en los valores actuales
    val recomendaciones = remember(humedadActual, temperaturaActual, humedadAireActual) {
        generarRecomendacionesAcciones(humedadActual, temperaturaActual, humedadAireActual)
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
            // Valores Actuales
            item {
                ValoresActualesCard(
                    humedad = humedadActual,
                    temperatura = temperaturaActual,
                    humedadAire = humedadAireActual
                )
            }
            
            // Resumen de Estado
            item {
                ResumenEstadoCard(
                    humedad = humedadActual,
                    temperatura = temperaturaActual,
                    humedadAire = humedadAireActual
                )
            }
            
            // Recomendaciones Prioritarias
            item {
                RecomendacionesPrioritariasCard(
                    recomendaciones = recomendaciones.filter { it.prioridad == Prioridad.ALTA }
                )
            }
            
            // Todas las Recomendaciones
            item {
                TodasRecomendacionesCard(
                    recomendaciones = recomendaciones
                )
            }
            
            // Plan de Acción
            item {
                PlanAccionCard(
                    recomendaciones = recomendaciones
                )
            }
            
            // Historial de Acciones
            item {
                HistorialAccionesCard()
            }
        }
    }
}

@Composable
fun ValoresActualesCard(
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
                text = "Valores Actuales de Sensores",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ValorSensorItem("Humedad", humedad, "%", Color(0xFF2E7D32))
                ValorSensorItem("Temperatura", temperatura, "°C", Color(0xFFD32F2F))
                ValorSensorItem("Humedad Aire", humedadAire, "%", Color(0xFF1976D2))
            }
        }
    }
}

@Composable
fun ValorSensorItem(
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
fun ResumenEstadoCard(
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
                text = "Resumen del Estado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            val estadoGeneral = determinarEstadoGeneral(humedad, temperatura, humedadAire)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (estadoGeneral.first) {
                        "Excelente" -> Icons.Default.CheckCircle
                        "Bueno" -> Icons.Default.CheckCircle
                        "Regular" -> Icons.Default.Warning
                        "Crítico" -> Icons.Default.Warning
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = estadoGeneral.second
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Estado General: ${estadoGeneral.first}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = estadoGeneral.second
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = estadoGeneral.third,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecomendacionesPrioritariasCard(
    recomendaciones: List<RecomendacionAccion>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🚨 Acciones Prioritarias",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            if (recomendaciones.isEmpty()) {
                Text(
                    text = "✅ No hay acciones prioritarias requeridas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32)
                )
            } else {
                recomendaciones.forEach { recomendacion ->
                    RecomendacionItem(recomendacion = recomendacion)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TodasRecomendacionesCard(
    recomendaciones: List<RecomendacionAccion>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 Todas las Recomendaciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            recomendaciones.forEach { recomendacion ->
                RecomendacionItem(recomendacion = recomendacion)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RecomendacionItem(
    recomendacion: RecomendacionAccion
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (recomendacion.prioridad) {
                Prioridad.ALTA -> Color(0xFFFFEBEE)
                Prioridad.MEDIA -> Color(0xFFFFF3E0)
                Prioridad.BAJA -> Color(0xFFE8F5E8)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (recomendacion.prioridad) {
                    Prioridad.ALTA -> Icons.Default.Warning
                    Prioridad.MEDIA -> Icons.Default.Lightbulb
                    Prioridad.BAJA -> Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = when (recomendacion.prioridad) {
                    Prioridad.ALTA -> Color(0xFFD32F2F)
                    Prioridad.MEDIA -> Color(0xFFFF9800)
                    Prioridad.BAJA -> Color(0xFF2E7D32)
                }
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recomendacion.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = recomendacion.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tiempo estimado: ${recomendacion.tiempoEstimado}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PlanAccionCard(
    recomendaciones: List<RecomendacionAccion>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📋 Plan de Acción Sugerido",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            val planOrdenado = recomendaciones.sortedBy { it.prioridad.ordinal }
            
            planOrdenado.forEachIndexed { index, recomendacion ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = recomendacion.titulo,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = { /* Marcar como completado */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Marcar Plan como Completado")
            }
        }
    }
}

@Composable
fun HistorialAccionesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Historial de Acciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            val historial = listOf(
                "Hace 2 horas: Riego aplicado (humedad baja)",
                "Hace 5 horas: Sombra colocada (temperatura alta)",
                "Ayer: Ventilación mejorada (humedad aire alta)",
                "Ayer: Fertilización aplicada"
            )
            
            historial.forEach { accion ->
                Text(
                    text = "• $accion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

fun determinarEstadoGeneral(humedad: Float, temperatura: Float, humedadAire: Float): Triple<String, Color, String> {
    val problemas = mutableListOf<String>()
    
    when {
        humedad < 20 -> problemas.add("Humedad crítica")
        humedad > 80 -> problemas.add("Exceso de humedad")
    }
    
    when {
        temperatura < 10 -> problemas.add("Temperatura muy baja")
        temperatura > 35 -> problemas.add("Temperatura muy alta")
    }
    
    when {
        humedadAire < 20 -> problemas.add("Aire muy seco")
        humedadAire > 80 -> problemas.add("Aire muy húmedo")
    }
    
    return when {
        problemas.isEmpty() -> Triple("Excelente", Color(0xFF2E7D32), "Todas las condiciones están óptimas")
        problemas.size == 1 -> Triple("Bueno", Color(0xFF4CAF50), "Solo un parámetro necesita atención")
        problemas.size == 2 -> Triple("Regular", Color(0xFFFF9800), "Algunos parámetros requieren atención")
        else -> Triple("Crítico", Color(0xFFD32F2F), "Múltiples parámetros críticos detectados")
    }
}

fun generarRecomendacionesAcciones(humedad: Float, temperatura: Float, humedadAire: Float): List<RecomendacionAccion> {
    val recomendaciones = mutableListOf<RecomendacionAccion>()
    
    // Recomendaciones basadas en humedad
    when {
        humedad < 20 -> recomendaciones.add(
            RecomendacionAccion(
                titulo = "Riego Urgente",
                descripcion = "El suelo está muy seco. Aplica riego moderado inmediatamente.",
                prioridad = Prioridad.ALTA,
                tiempoEstimado = "15-20 minutos"
            )
        )
        humedad in 20f..30f -> recomendaciones.add(
            RecomendacionAccion(
                titulo = "Riego Preventivo",
                descripcion = "El suelo está seco. Considera un riego ligero.",
                prioridad = Prioridad.MEDIA,
                tiempoEstimado = "10-15 minutos"
            )
        )
        humedad > 80 -> recomendaciones.add(
            RecomendacionAccion(
                titulo = "Suspender Riego",
                descripcion = "Humedad excesiva. Suspende el riego y mejora el drenaje.",
                prioridad = Prioridad.ALTA,
                tiempoEstimado = "Inmediato"
            )
        )
    }
    
    // Recomendaciones basadas en temperatura
    when {
        temperatura > 35 -> recomendaciones.add(
            RecomendacionAccion(
                titulo = "Protección contra Calor",
                descripcion = "Temperatura muy alta. Coloca sombra y aumenta ventilación.",
                prioridad = Prioridad.ALTA,
                tiempoEstimado = "30 minutos"
            )
        )
        temperatura < 10 -> recomendaciones.add(
            RecomendacionAccion(
                titulo = "Protección contra Frío",
                descripcion = "Temperatura muy baja. Protege las plantas del frío.",
                prioridad = Prioridad.ALTA,
                tiempoEstimado = "20 minutos"
            )
        )
    }
    
    // Recomendaciones basadas en humedad del aire
    when {
        humedadAire < 30 -> recomendaciones.add(
            RecomendacionAccion(
                titulo = "Humidificar Ambiente",
                descripcion = "Aire muy seco. Considera humidificar el ambiente.",
                prioridad = Prioridad.MEDIA,
                tiempoEstimado = "45 minutos"
            )
        )
        humedadAire > 80 -> recomendaciones.add(
            RecomendacionAccion(
                titulo = "Mejorar Ventilación",
                descripcion = "Aire muy húmedo. Mejora la ventilación del área.",
                prioridad = Prioridad.MEDIA,
                tiempoEstimado = "30 minutos"
            )
        )
    }
    
    // Recomendaciones generales
    if (humedad in 30f..60f && temperatura in 15f..30f && humedadAire in 40f..70f) {
        recomendaciones.add(
            RecomendacionAccion(
                titulo = "Mantenimiento Preventivo",
                descripcion = "Condiciones óptimas. Realiza mantenimiento preventivo.",
                prioridad = Prioridad.BAJA,
                tiempoEstimado = "1 hora"
            )
        )
    }
    
    return recomendaciones
}

data class RecomendacionAccion(
    val titulo: String,
    val descripcion: String,
    val prioridad: Prioridad,
    val tiempoEstimado: String
)

enum class Prioridad {
    ALTA, MEDIA, BAJA
}
