package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kaquenduri.prueba01_mqtt.ViewModels.SensorViewModel
import com.kaquenduri.prueba01_mqtt.ViewModels.CultivoViewModel
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCultivoScreen(
    cultivoId: Int,
    navController: NavController,
    sensorViewModel: SensorViewModel
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val cultivoDao = db.cultivoDao()

    // Obtener el cultivo actual
    val cultivoActual by cultivoDao.obtenerCultivoPorId(cultivoId)
        .collectAsState(initial = null)

    // Inicializar simulación de sensores cuando se carga el cultivo
    LaunchedEffect(cultivoActual) {
        cultivoActual?.let { cultivo ->
            sensorViewModel.onCultivoChange(cultivo.nombre)
            
            // Determinar sensores simulados activos
            val sensoresSimulados = mutableSetOf<String>()
            if (cultivo.sensorPh) sensoresSimulados.add("ph")
            if (cultivo.sensorConductividad) sensoresSimulados.add("conductividad")
            if (cultivo.sensorNutrientes) sensoresSimulados.add("nutrientes")
            if (cultivo.sensorLuz) sensoresSimulados.add("luz")
            
            sensorViewModel.iniciarSimulacionSensores(cultivoId, sensoresSimulados)
        }
    }

    // Limpiar simulación al salir
    DisposableEffect(Unit) {
        onDispose {
            sensorViewModel.detenerSimulacionSensores()
        }
    }

    // Estados de sensores reales
    val mensajeH by sensorViewModel.mensajeHumedad
    val mensajeT by sensorViewModel.mensajeTemperatura
    val mensajeHA by sensorViewModel.mensajeHumedadAire
    val estadoConexion by sensorViewModel.estadoConexion
    val conectado by sensorViewModel.conectado

    // Estados de sensores simulados
    val phSuelo by sensorViewModel.phSuelo
    val conductividad by sensorViewModel.conductividad
    val nitrogeno by sensorViewModel.nitrogeno
    val fosforo by sensorViewModel.fosforo
    val potasio by sensorViewModel.potasio
    val intensidadLuz by sensorViewModel.intensidadLuz

    // Extraer valores numéricos
    val humedadActual = remember(mensajeH) {
        Regex("""\d+""").find(mensajeH)?.value?.toFloatOrNull() ?: 0f
    }
    val humedadAireActual = remember(mensajeHA) {
        Regex("""\d+""").find(mensajeHA)?.value?.toFloatOrNull() ?: 0f
    }
    val temperaturaActual = remember(mensajeT) {
        Regex("""\d+""").find(mensajeT)?.value?.toFloatOrNull() ?: 0f
    }

    Scaffold(
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado de conexión
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (conectado) 
                            Color(0xFF2E7D32).copy(alpha = 0.1f) 
                        else 
                            Color(0xFFD32F2F).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estado de Conexión",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = estadoConexion,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (conectado) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { sensorViewModel.iniciarConexion() },
                                enabled = !conectado,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Text("Conectar")
                            }
                            Button(
                                onClick = { sensorViewModel.desconectar() },
                                enabled = conectado,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F)
                                )
                            ) {
                                Text("Desconectar")
                            }
                        }
                    }
                }
            }

            cultivoActual?.let { cultivo ->
                // ========== SENSORES REALES ==========
                if (cultivo.sensorHumedadSuelo || cultivo.sensorTemperatura || cultivo.sensorHumedadAire) {
                    item {
                        Text(
                            text = "Sensores Reales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Sensor Humedad Suelo (Real)
                    if (cultivo.sensorHumedadSuelo) {
                        item {
                            SensorCard(
                                titulo = "💧 Humedad del Suelo",
                                mensaje = mensajeH,
                                valor = humedadActual,
                                esReal = true,
                                badge = "EN VIVO"
                            )
                        }
                    }

                    // Sensor Temperatura (Real)
                    if (cultivo.sensorTemperatura) {
                        item {
                            SensorCard(
                                titulo = "🌡️ Temperatura Ambiente",
                                mensaje = mensajeT,
                                valor = temperaturaActual,
                                esReal = true,
                                badge = "EN VIVO"
                            )
                        }
                    }

                    // Sensor Humedad Aire (Real)
                    if (cultivo.sensorHumedadAire) {
                        item {
                            SensorCard(
                                titulo = "💨 Humedad del Aire",
                                mensaje = mensajeHA,
                                valor = humedadAireActual,
                                esReal = true,
                                badge = "EN VIVO"
                            )
                        }
                    }
                }

                // ========== SENSORES SIMULADOS ==========
                if (cultivo.sensorPh || cultivo.sensorConductividad || cultivo.sensorNutrientes || cultivo.sensorLuz) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sensores Simulados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Sensor pH (Simulado)
                    if (cultivo.sensorPh) {
                        item {
                            SensorCard(
                                titulo = "🧪 pH del Suelo",
                                mensaje = sensorViewModel.obtenerMensajePh(),
                                valor = phSuelo,
                                esReal = false,
                                badge = "SIMULADO",
                                esPh = true
                            )
                        }
                    }

                    // Sensor Conductividad (Simulado)
                    if (cultivo.sensorConductividad) {
                        item {
                            SensorCard(
                                titulo = "⚡ Conductividad Eléctrica",
                                mensaje = sensorViewModel.obtenerMensajeConductividad(),
                                valor = conductividad,
                                esReal = false,
                                badge = "SIMULADO",
                                esPh = false
                            )
                        }
                    }

                    // Sensor Nutrientes NPK (Simulado)
                    if (cultivo.sensorNutrientes) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🧪 Nutrientes NPK",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Badge(
                                            containerColor = Color(0xFF9E9E9E),
                                            contentColor = Color.White
                                        ) {
                                            Text("SIMULADO", fontSize = 10.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = sensorViewModel.obtenerMensajeNutrientes(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        NutrientIndicator("N", nitrogeno, 50, 80)
                                        NutrientIndicator("P", fosforo, 20, 35)
                                        NutrientIndicator("K", potasio, 150, 200)
                                    }
                                }
                            }
                        }
                    }

                    // Sensor Luz (Simulado)
                    if (cultivo.sensorLuz) {
                        item {
                            SensorCard(
                                titulo = "☀️ Intensidad Lumínica",
                                mensaje = sensorViewModel.obtenerMensajeLuz(),
                                valor = intensidadLuz.toFloat(),
                                esReal = false,
                                badge = "SIMULADO"
                            )
                        }
                    }
                }

                // Navegación contextual
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gestión del Cultivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    val cultivoViewModel: CultivoViewModel = viewModel()
                    val scope = rememberCoroutineScope()
                    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigate("editarCultivo/$cultivoId") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar")
                        }
                        OutlinedButton(
                            onClick = { mostrarDialogoEliminar = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    }

                    // Diálogo de confirmación de eliminación
                    if (mostrarDialogoEliminar) {
                        AlertDialog(
                            onDismissRequest = { mostrarDialogoEliminar = false },
                            title = { Text("¿Eliminar cultivo?") },
                            text = { 
                                Text("Esta acción eliminará el cultivo y todas sus actividades asociadas. Esta acción no se puede deshacer.") 
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            cultivoViewModel.initRepository(context)
                                            cultivoViewModel.eliminarCultivo(cultivoId)
                                            mostrarDialogoEliminar = false
                                            navController.navigate("listaCultivos") {
                                                popUpTo("listaCultivos") { inclusive = true }
                                            }
                                        }
                                    }
                                ) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Monitoreo y Datos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("estadisticas/$cultivoId") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Estadísticas")
                        }
                        Button(
                            onClick = { navController.navigate("graficos/$cultivoId") },
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
                            onClick = { navController.navigate("registroActividad/$cultivoId") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Actividades")
                        }
                        Button(
                            onClick = { navController.navigate("ia_chat?cultivoId=$cultivoId") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("IA Chat")
                        }
                    }
                }
            } ?: run {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cultivo no encontrado",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ID: $cultivoId",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorCard(
    titulo: String,
    mensaje: String,
    valor: Float,
    esReal: Boolean,
    badge: String,
    esPh: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esReal) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = if (esReal) Color(0xFF2E7D32) else Color(0xFF9E9E9E),
                    contentColor = Color.White
                ) {
                    Text(badge, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (esPh) {
                // Para pH, mostrar valor directamente (no porcentaje)
                Text(
                    text = "${String.format("%.1f", valor)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                SensorCircularIndicator(valor)
            }
        }
    }
}

@Composable
fun SensorCircularIndicatorv2(datoSensor: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (datoSensor > 100) datoSensor / 100f else datoSensor / 100f
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
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
                sweepAngle = 360f * animatedProgress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(14f, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${datoSensor.roundToInt()}${if (datoSensor > 100) "" else "%"}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NutrientIndicator(
    label: String,
    valor: Int,
    min: Int,
    max: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$valor",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                valor < min -> Color(0xFFD32F2F)
                valor > max -> Color(0xFF0288D1)
                else -> Color(0xFF2E7D32)
            }
        )
        Text(
            text = "ppm",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

