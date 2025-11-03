package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaquenduri.prueba01_mqtt.ViewModels.SensorViewModel
import com.kaquenduri.prueba01_mqtt.utils.mostrarNotificacion
import kotlinx.coroutines.launch
import androidx.navigation.NavController

import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SensorViewModel,
    navController: NavController,
    irAlertas: () -> Unit,
    irConfiguracion: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mensajeH by viewModel.mensajeHumedad
    val mensajeT by viewModel.mensajeTemperatura
    val mensajeHA by viewModel.mensajeHumedadAire
    val context = LocalContext.current
    val alertaAltaHumedad by viewModel.alertaHumedadAlta
    val alertaBajaHumedad by viewModel.alertaHumedadBaja
    val estadoConexion by viewModel.estadoConexion
    val conectado by viewModel.conectado
    val cultivo by viewModel.cultivo.collectAsState()


    // 🌧️ Extraer porcentaje numérico del mensaje
    val humedadActual = remember(mensajeH) {
        Regex("""\d+""").find(mensajeH)?.value?.toFloatOrNull() ?: 0f
    }
    val humedadAireActual = remember(mensajeHA) {
        Regex("""\d+""").find(mensajeHA)?.value?.toFloatOrNull() ?: 0f
    }
    val temperaturaActual = remember(mensajeT) {
        Regex("""\d+""").find(mensajeT)?.value?.toFloatOrNull() ?: 0f
    }



    // 📊 Historial de humedad (simulado por ahora)
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

    LazyColumn(
        Modifier
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

        item {
            var expanded by remember { mutableStateOf(false) }

            Text("Selecciona tu cultivo", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            Box {
                OutlinedTextField(
                    value = cultivo,
                    onValueChange = {},
                    label = { Text("Tipo de cultivo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    readOnly = true
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Tomates", "Lechuga", "Fresas", "Papas", "Maíz", "Cebolla").forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                viewModel.onCultivoChange(tipo)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        item {
            // 🌿 Indicador circulares
            SensorCircularIndicator(humedadActual)
            Text("Humedad")
            Spacer(Modifier.height(24.dp))

            SensorCircularIndicator(humedadAireActual)
            Text("Humedad del Aire")
            Spacer(Modifier.height(24.dp))

            SensorCircularIndicator(temperaturaActual)
            Text("Temperatura del Aire")
            Spacer(Modifier.height(24.dp))
        }


        item{
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



//            item{
//                // 📈 Gráfico simple de historial de humedad
//                Text("Historial reciente de humedad (%)", fontWeight = FontWeight.Medium)
//                Spacer(Modifier.height(12.dp))
//                GraficoHumedad(historial = historialHumedad)
//
//                Spacer(Modifier.height(32.dp))
//            }

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
            Button(onClick = { navController.navigate("cultivo") }) {
                Text("Ver cultivos")
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
