package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaquenduri.prueba01_mqtt.utils.mostrarNotificacion
import androidx.navigation.NavController
import com.kaquenduri.prueba01_mqtt.ViewModels.SensorViewModel
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SensorViewModel,
    navController: NavController,
    cultivoId: Int, // 👈 NUEVO PARÁMETRO OBLIGATORIO
    irAlertas: () -> Unit,
    irConfiguracion: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val cultivoDao = db.cultivoDao()

    // Obtener el cultivo actual
    val cultivoActual by cultivoDao.obtenerCultivoPorId(cultivoId)
        .collectAsState(initial = null)

    // Pasar el cultivo al ViewModel
    LaunchedEffect(cultivoActual) {
        cultivoActual?.let { cultivo ->
            viewModel.onCultivoChange(cultivo.nombre)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mensajeH by viewModel.mensajeHumedad
    val mensajeT by viewModel.mensajeTemperatura
    val mensajeHA by viewModel.mensajeHumedadAire
    val alertaAltaHumedad by viewModel.alertaHumedadAlta
    val alertaBajaHumedad by viewModel.alertaHumedadBaja
    val estadoConexion by viewModel.estadoConexion
    val conectado by viewModel.conectado
    val cultivoSeleccionado by viewModel.cultivo.collectAsState()

    //  Extraer porcentaje numérico del mensaje
    val humedadActual = remember(mensajeH) {
        Regex("""\d+""").find(mensajeH)?.value?.toFloatOrNull() ?: 0f
    }
    val humedadAireActual = remember(mensajeHA) {
        Regex("""\d+""").find(mensajeHA)?.value?.toFloatOrNull() ?: 0f
    }
    val temperaturaActual = remember(mensajeT) {
        Regex("""\d+""").find(mensajeT)?.value?.toFloatOrNull() ?: 0f
    }

    //  Historial de humedad (simulado por ahora)
    var historialHumedad by remember { mutableStateOf(listOf<Float>()) }

    LaunchedEffect(humedadActual) {
        if (humedadActual > 0f) {
            historialHumedad = (historialHumedad + humedadActual).takeLast(10)
        }
    }

    LaunchedEffect(alertaAltaHumedad, alertaBajaHumedad) {
        if (alertaAltaHumedad) mostrarNotificacion(context, "¡Humedad muy alta! Suspende el riego.")
        if (alertaBajaHumedad) mostrarNotificacion(context, "¡Suelo muy seco! Riega con cuidado.")
    }

    Scaffold(
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // 🔌 Estado de conexión
            item{
                Text(
                    text = "Estado: $estadoConexion",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (conectado) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
                Spacer(Modifier.height(24.dp))
            }

            // SOLO mostrar sensores que están activos para este cultivo
            cultivoActual?.let { cultivo ->
                // Sensor Humedad Suelo
                if (cultivo.sensorHumedadSuelo) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("💧 Humedad del Suelo", style = MaterialTheme.typography.titleMedium)
                                Text(viewModel.mensajeHumedad.value)

                                // 🌿 Indicador circular para humedad suelo
                                SensorCircularIndicator(humedadActual)
                                Text("Humedad del Suelo")
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }

                // Sensor Temperatura
                if (cultivo.sensorTemperatura) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("🌡️ Temperatura", style = MaterialTheme.typography.titleMedium)
                                Text(viewModel.mensajeTemperatura.value)

                                // 🌡️ Indicador circular para temperatura
                                SensorCircularIndicator(temperaturaActual)
                                Text("Temperatura del Aire")
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }

                // Sensor Humedad Aire
                if (cultivo.sensorHumedadAire) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("💨 Humedad del Aire", style = MaterialTheme.typography.titleMedium)
                                Text(viewModel.mensajeHumedadAire.value)

                                // 💨 Indicador circular para humedad aire
                                SensorCircularIndicator(humedadAireActual)
                                Text("Humedad del Aire")
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }

                // Mostrar sensores no activados
                item {
                    val sensoresInactivos = mutableListOf<String>()
                    if (!cultivo.sensorHumedadSuelo) sensoresInactivos.add("Humedad Suelo")
                    if (!cultivo.sensorTemperatura) sensoresInactivos.add("Temperatura")
                    if (!cultivo.sensorHumedadAire) sensoresInactivos.add("Humedad Aire")
                    if (!cultivo.sensorLuz) sensoresInactivos.add("Luz")
                    if (!cultivo.sensorPh) sensoresInactivos.add("pH")

                    if (sensoresInactivos.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("📊 Sensores No Activos", style = MaterialTheme.typography.titleSmall)
                                Text(sensoresInactivos.joinToString(", "), fontSize = 14.sp)
                                Text("Puedes activarlos en la gestión de cultivos", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

            } ?: run {
                item {
                    Text("Cultivo no encontrado - ID: $cultivoId")
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                // 💬 Mensaje contextual
                Text(
                    text = when {
                        humedadActual == 0f -> "Esperando datos del sensor..."
                        humedadActual < 30 -> "El suelo está muy seco 🌵"
                        humedadActual in 30f..60f -> "Nivel de humedad óptimo 🌱"
                        else -> "Demasiada humedad 💧"
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(32.dp))
            }

            item{
                // ⚙️ Botones de conexión
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.iniciarConexion() },
                        enabled = !conectado,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Conectar")
                    }

                    Button(
                        onClick = { viewModel.desconectar() },
                        enabled = conectado,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Desconectar")
                    }
                }
            }

            item {
                // Botones de navegación a nuevas funcionalidades
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("cultivo") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cultivos")
                    }
                    Button(
                        onClick = { navController.navigate("graficos") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Gráficos")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("estadisticas") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Estadísticas")
                    }
                    Button(
                        onClick = { navController.navigate("limites") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Límites")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("alertas_sonoras") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sonidos")
                    }
                    Button(
                        onClick = { navController.navigate("ordenar_modulos") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ordenar")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("notificaciones_push") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Notificaciones")
                    }
                    Button(
                        onClick = { navController.navigate("recomendaciones_acciones") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Acciones")
                    }
                }
            }
        }
    }
}

@Composable
fun SensorCircularIndicator(datoSensor: Float) {
    val animatedProgress by animateFloatAsState(targetValue = datoSensor / 100f)
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawArc(
                color = Color(0xFFBDBDBD),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(14f, cap = StrokeCap.Round)
            )
            drawArc(
                color = when {
                    datoSensor < 30 -> Color(0xFFD32F2F)
                    datoSensor <= 60 -> Color(0xFF2E7D32)
                    else -> Color(0xFF0288D1)
                },
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(14f, cap = StrokeCap.Round)
            )
        }
        Text("${datoSensor.roundToInt()}%", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GraficoHumedad(historial: List<Float>) {
    if (historial.isEmpty()) {
        Text("Sin datos aún...")
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val maxH = historial.maxOrNull() ?: 100f
        val stepX = size.width / (historial.size - 1)
        for (i in 0 until historial.lastIndex) {
            val x1 = i * stepX
            val x2 = (i + 1) * stepX
            val y1 = size.height - (historial[i] / maxH) * size.height
            val y2 = size.height - (historial[i + 1] / maxH) * size.height
            drawLine(
                color = Color(0xFF2E7D32),
                start = androidx.compose.ui.geometry.Offset(x1, y1),
                end = androidx.compose.ui.geometry.Offset(x2, y2),
                strokeWidth = 6f
            )
        }
    }
}