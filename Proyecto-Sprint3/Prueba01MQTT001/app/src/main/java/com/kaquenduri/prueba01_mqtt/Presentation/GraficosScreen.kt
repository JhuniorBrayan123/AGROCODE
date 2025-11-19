package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import android.graphics.Paint
import com.google.firebase.firestore.FirebaseFirestore
import com.kaquenduri.prueba01_mqtt.ViewModels.SensorViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

// Enum para el tipo de tiempo
enum class TipoTiempo(val label: String) {
    HORAS("Horas"),
    DIAS("Días"),
    SEMANAS("Semanas")
}

// Enum para rangos
enum class Rango(val label: String, val color: Color) {
    BAJO("Bajo", Color(0xFFD32F2F)),
    OPTIMO("Óptimo", Color(0xFF2E7D32)),
    ALTO("Alto", Color(0xFFFF9800))
}

// Data class para datos de sensores
data class SensorData(
    val timestamp: Long,
    val humedad: Float,
    val humedadAire: Float,
    val ph: Float? = null,
    val conductividad: Float? = null,
    val intensidadLuz: Int? = null,
    val esManual: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraficosScreen(
    viewModel: SensorViewModel,
    irHome: () -> Unit,
    cultivoId: Int = 0
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estados de sensores reales
    val mensajeH by viewModel.mensajeHumedad
    val mensajeT by viewModel.mensajeTemperatura
    val mensajeHA by viewModel.mensajeHumedadAire
    
    // Estados de sensores simulados
    val phSuelo by viewModel.phSuelo
    val conductividad by viewModel.conductividad
    val intensidadLuz by viewModel.intensidadLuz
    
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
    
    // Estados de UI
    var tipoTiempoSeleccionado by remember { mutableStateOf(TipoTiempo.HORAS) }
    var mostrarDialogoEntradaManual by remember { mutableStateOf(false) }
    var puntoSeleccionado by remember { mutableStateOf<SensorData?>(null) }
    
    // Datos históricos combinados
    var datosHistoricos by remember { mutableStateOf<List<SensorData>>(emptyList()) }
    
    // Cargar datos de Firestore
    LaunchedEffect(cultivoId, tipoTiempoSeleccionado) {
        try {
            val collection = firestore.collection("DatosSensores")
                .whereEqualTo("cultivoId", cultivoId)
                .orderBy("timestamp")
            
            val snapshot = collection.get().await()
            datosHistoricos = snapshot.documents.map { doc ->
                SensorData(
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                    humedad = (doc.getDouble("humedad") ?: 0.0).toFloat(),
                    humedadAire = (doc.getDouble("humedadAire") ?: 0.0).toFloat(),
                    ph = doc.getDouble("ph")?.toFloat(),
                    conductividad = doc.getDouble("conductividad")?.toFloat(),
                    intensidadLuz = doc.getLong("intensidadLuz")?.toInt(),
                    esManual = doc.getBoolean("esManual") ?: false
                )
            }
        } catch (e: Exception) {
            // Si no hay datos, usar datos actuales
            datosHistoricos = emptyList()
        }
    }
    
    // Actualizar datos históricos con datos actuales
    LaunchedEffect(humedadActual, humedadAireActual, phSuelo, conductividad, intensidadLuz) {
        if (humedadActual > 0f || humedadAireActual > 0f) {
            val nuevoDato = SensorData(
                timestamp = System.currentTimeMillis(),
                humedad = humedadActual,
                humedadAire = humedadAireActual,
                ph = phSuelo,
                conductividad = conductividad,
                intensidadLuz = intensidadLuz,
                esManual = false
            )
            datosHistoricos = (datosHistoricos + nuevoDato).takeLast(50)
        }
    }
    
    // Función para guardar datos manuales en Firestore
    fun guardarDatoManual(
        ph: Float?,
        conductividad: Float?,
        intensidadLuz: Int?,
        timestamp: Long = System.currentTimeMillis()
    ) {
        scope.launch {
            try {
                val datoManual = hashMapOf(
                    "cultivoId" to cultivoId,
                    "timestamp" to timestamp,
                    "humedad" to humedadActual,
                    "humedadAire" to humedadAireActual,
                    "ph" to (ph ?: phSuelo),
                    "conductividad" to (conductividad ?: conductividad),
                    "intensidadLuz" to (intensidadLuz ?: intensidadLuz),
                    "esManual" to true
                )

                firestore.collection("DatosSensores")
                    .add(datoManual)
                    .await()

                // Actualizar datos locales
                val nuevoDato = SensorData(
                    timestamp = timestamp,
                    humedad = humedadActual,
                    humedadAire = humedadAireActual,
                    ph = ph,
                    conductividad = conductividad,
                    intensidadLuz = intensidadLuz,
                    esManual = true
                )
                datosHistoricos = (datosHistoricos + nuevoDato).takeLast(50)

                snackbarHostState.showSnackbar(
                    "Datos guardados exitosamente",
                    duration = SnackbarDuration.Short
                )
                mostrarDialogoEntradaManual = false
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Error al guardar: ${e.message}",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoEntradaManual = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar dato manual")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con temperatura en círculo y selector de tiempo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Selector de tiempo
                        Column {
                            Text(
                                text = "Período de Tiempo",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TipoTiempo.values().forEach { tipo ->
                                    FilterChip(
                                        selected = tipoTiempoSeleccionado == tipo,
                                        onClick = { tipoTiempoSeleccionado = tipo },
                                        label = { Text(tipo.label) }
                                    )
                                }
                            }
                        }
                        
                        // Temperatura en círculo
                        TemperaturaCircular(temperaturaActual)
                    }
                }
            }
            
            // Gráfica unificada
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Gráfica Unificada de Sensores",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Gráfica con rangos
                        GraficaUnificada(
                            datos = datosHistoricos,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        )
                        
                        // Leyenda
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LeyendaItem("Humedad", Color(0xFF2E7D32))
                            LeyendaItem("Humedad Aire", Color(0xFF1976D2))
                            LeyendaItem("pH", Color(0xFF9C27B0))
                            LeyendaItem("Conductividad", Color(0xFFFF9800))
                            LeyendaItem("Luz", Color(0xFFFFEB3B))
                        }
                        
                        // Rangos en el eje Y
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Rango.values().forEach { rango ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(rango.color, CircleShape)
                                    )
                                    Text(
                                        text = rango.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = rango.color,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo para entrada manual de datos
    if (mostrarDialogoEntradaManual) {
        DialogoEntradaManual(
            onDismiss = { mostrarDialogoEntradaManual = false },
            onGuardar = { ph, conductividad, luz ->
                guardarDatoManual(ph, conductividad, luz)
            },
            phInicial = phSuelo,
            conductividadInicial = conductividad,
            luzInicial = intensidadLuz
        )
    }
}

@Composable
fun TemperaturaCircular(temperatura: Float) {
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = (temperatura / 50f).coerceIn(0f, 1f),
        label = "temperatura"
    )
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Círculo de fondo
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = size.minDimension / 2 - 10,
                style = Stroke(12f)
            )
            
            // Círculo de temperatura
            drawArc(
                color = when {
                    temperatura < 15 -> Color(0xFF2196F3)
                    temperatura > 30 -> Color(0xFFD32F2F)
                    else -> Color(0xFF2E7D32)
                },
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(12f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${temperatura.roundToInt()}°C",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    temperatura < 15 -> Color(0xFF2196F3)
                    temperatura > 30 -> Color(0xFFD32F2F)
                    else -> Color(0xFF2E7D32)
                }
            )
            Text(
                text = "Temperatura",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GraficaUnificada(
    datos: List<SensorData>,
    modifier: Modifier = Modifier
) {
    if (datos.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Esperando datos de sensores...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    Canvas(modifier = modifier) {
        val padding = 40f
        val graphWidth = size.width - padding * 2
        val graphHeight = size.height - padding * 2
        
        // Calcular rangos
        val maxHumedad = datos.maxOfOrNull { it.humedad } ?: 100f
        val minHumedad = datos.minOfOrNull { it.humedad } ?: 0f
        val maxHumedadAire = datos.maxOfOrNull { it.humedadAire } ?: 100f
        val minHumedadAire = datos.minOfOrNull { it.humedadAire } ?: 0f
        
        // Rangos óptimos (para mostrar zonas)
        val rangoOptimoMin = 30f
        val rangoOptimoMax = 70f
        
        // Dibujar zonas de rango
        val yOptimoMin = padding + graphHeight - ((rangoOptimoMin - minHumedad) / (maxHumedad - minHumedad)) * graphHeight
        val yOptimoMax = padding + graphHeight - ((rangoOptimoMax - minHumedad) / (maxHumedad - minHumedad)) * graphHeight
        
        // Zona óptima
        drawRect(
            color = Rango.OPTIMO.color.copy(alpha = 0.1f),
            topLeft = Offset(padding, yOptimoMax),
            size = androidx.compose.ui.geometry.Size(graphWidth, yOptimoMin - yOptimoMax)
        )
        
        // Dibujar líneas de referencia
        drawLine(
            color = Rango.OPTIMO.color.copy(alpha = 0.3f),
            start = Offset(padding, yOptimoMin),
            end = Offset(padding + graphWidth, yOptimoMin),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
        )
        drawLine(
            color = Rango.OPTIMO.color.copy(alpha = 0.3f),
            start = Offset(padding, yOptimoMax),
            end = Offset(padding + graphWidth, yOptimoMax),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
        )
        
        // Dibujar líneas de datos
        val stepX = if (datos.size > 1) graphWidth / (datos.size - 1) else 0f
        
        // Humedad del suelo
        datos.forEachIndexed { index, dato ->
            if (index < datos.size - 1) {
                val x1 = padding + index * stepX
                val x2 = padding + (index + 1) * stepX
                val y1 = padding + graphHeight - ((dato.humedad - minHumedad) / (maxHumedad - minHumedad)) * graphHeight
                val y2 = padding + graphHeight - ((datos[index + 1].humedad - minHumedad) / (maxHumedad - minHumedad)) * graphHeight
                
                drawLine(
                    color = Color(0xFF2E7D32),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f
                )
            }
        }
        
        // Humedad del aire
        datos.forEachIndexed { index, dato ->
            if (index < datos.size - 1) {
                val x1 = padding + index * stepX
                val x2 = padding + (index + 1) * stepX
                val y1 = padding + graphHeight - ((dato.humedadAire - minHumedadAire) / (maxHumedadAire - minHumedadAire)) * graphHeight
                val y2 = padding + graphHeight - ((datos[index + 1].humedadAire - minHumedadAire) / (maxHumedadAire - minHumedadAire)) * graphHeight
                
                drawLine(
                    color = Color(0xFF1976D2),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f
                )
            }
        }
        
        // Dibujar puntos para datos manuales
        datos.forEachIndexed { index, dato ->
            if (dato.esManual) {
                val x = padding + index * stepX
                val y = padding + graphHeight - ((dato.humedad - minHumedad) / (maxHumedad - minHumedad)) * graphHeight
                
                drawCircle(
                    color = Color(0xFFFF0000),
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
        }
        
        // Etiquetas del eje Y (usando drawIntoCanvas para mejor compatibilidad)
        drawIntoCanvas { canvas ->
            val labels = listOf("100", "75", "50", "25", "0")
            labels.forEachIndexed { index, label ->
                val y = padding + (graphHeight / 4) * index
                val paint = Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    textAlign = Paint.Align.RIGHT
                }
                canvas.nativeCanvas.drawText(label, padding - 10f, y + 8f, paint)
            }
        }
    }
}

@Composable
fun LeyendaItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEntradaManual(
    onDismiss: () -> Unit,
    onGuardar: (ph: Float?, conductividad: Float?, luz: Int?) -> Unit,
    phInicial: Float,
    conductividadInicial: Float,
    luzInicial: Int
) {
    var ph by remember { mutableStateOf(phInicial.toString()) }
    var conductividad by remember { mutableStateOf(conductividadInicial.toString()) }
    var luz by remember { mutableStateOf(luzInicial.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar Datos Manuales",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = ph,
                    onValueChange = { ph = it },
                    label = { Text("pH del Suelo") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: 6.5") }
                )
                
                OutlinedTextField(
                    value = conductividad,
                    onValueChange = { conductividad = it },
                    label = { Text("Conductividad (mS/cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: 1.2") }
                )
                
                OutlinedTextField(
                    value = luz,
                    onValueChange = { luz = it },
                    label = { Text("Intensidad Lumínica (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: 75") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val phValue = ph.toFloatOrNull()
                    val conductividadValue = conductividad.toFloatOrNull()
                    val luzValue = luz.toIntOrNull()
                    onGuardar(phValue, conductividadValue, luzValue)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
