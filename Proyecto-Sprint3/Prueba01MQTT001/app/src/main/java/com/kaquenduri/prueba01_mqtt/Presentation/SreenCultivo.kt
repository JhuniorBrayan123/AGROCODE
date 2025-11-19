package com.kaquenduri.prueba01_mqtt.Presentation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.NavController
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.Cultivo
import com.kaquenduri.prueba01_mqtt.ViewModels.CultivoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCultivo(
    navController: NavController,
    userId: Int = 1,
    cultivoViewModel: CultivoViewModel = viewModel()
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val cultivoDao = db.cultivoDao()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        cultivoViewModel.initRepository(context)
    }

    // Estados de formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }

    // SENSORES REALES (Firebase/MQTT)
    var sensorHumedadSuelo by remember { mutableStateOf(false) }
    var sensorTemperatura by remember { mutableStateOf(false) }
    var sensorHumedadAire by remember { mutableStateOf(false) }

    // SENSORES SIMULADOS (Generación automática)
    var sensorPh by remember { mutableStateOf(false) }
    var sensorConductividad by remember { mutableStateOf(false) }
    var sensorNutrientes by remember { mutableStateOf(false) }
    var sensorLuz by remember { mutableStateOf(false) }

    // Lista reactiva de cultivos desde la BD
    val listaCultivos by cultivoDao
        .obtenerTodosCultivosPorUsuario(userId)
        .collectAsState(initial = emptyList())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Campos básicos
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Cultivo *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = tipo,
                onValueChange = { tipo = it },
                label = { Text("Tipo *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Hortaliza, Frutal, Ornamental") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = especie,
                onValueChange = { especie = it },
                label = { Text("Especie *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Tomate, Lechuga, Pimiento") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ========== SENSORES REALES ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9).copy(alpha = 0.3f)
                )
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 })
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sensores Reales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ) {
                                Text("EN VIVO", fontSize = 10.sp)
                            }
                            IconButton(onClick = { /* TODO: Mostrar info */ }) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Datos en tiempo real desde sensores físicos conectados (MQTT/Firebase)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = sensorHumedadSuelo,
                            onClick = { sensorHumedadSuelo = !sensorHumedadSuelo },
                            label = { Text("💧 Humedad Suelo") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = sensorTemperatura,
                            onClick = { sensorTemperatura = !sensorTemperatura },
                            label = { Text("🌡️ Temperatura") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = sensorHumedadAire,
                            onClick = { sensorHumedadAire = !sensorHumedadAire },
                            label = { Text("💨 Humedad Aire") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== SENSORES SIMULADOS ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD).copy(alpha = 0.3f)
                )
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 })
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sensores Simulados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = Color(0xFF9E9E9E),
                                contentColor = Color.White
                            ) {
                                Text("SIMULADO", fontSize = 10.sp)
                            }
                            IconButton(onClick = { /* TODO: Mostrar info */ }) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Datos generados automáticamente con valores realistas para pruebas y desarrollo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = sensorPh,
                            onClick = { sensorPh = !sensorPh },
                            label = { Text("🧪 pH") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = sensorConductividad,
                            onClick = { sensorConductividad = !sensorConductividad },
                            label = { Text("⚡ Conductividad") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = sensorNutrientes,
                            onClick = { sensorNutrientes = !sensorNutrientes },
                            label = { Text("🧪 Nutrientes NPK") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = sensorLuz,
                            onClick = { sensorLuz = !sensorLuz },
                            label = { Text("☀️ Luz") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de crear
            Button(
                onClick = {
                    if (nombre.isNotBlank() && tipo.isNotBlank() && especie.isNotBlank()) {
                        scope.launch {
                            val fechaActual = SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(Date())

                            val cultivo = Cultivo(
                                nombre = nombre,
                                descripcion = descripcion,
                                tipo = tipo,
                                especie = especie,
                                fechaSiembra = fechaActual,
                                idUsuario = userId,
                                // Sensores reales
                                sensorHumedadSuelo = sensorHumedadSuelo,
                                sensorTemperatura = sensorTemperatura,
                                sensorHumedadAire = sensorHumedadAire,
                                // Sensores simulados
                                sensorPh = sensorPh,
                                sensorConductividad = sensorConductividad,
                                sensorNutrientes = sensorNutrientes,
                                sensorLuz = sensorLuz
                            )
                            cultivoViewModel.insertarCultivo(cultivo)
                            
                            snackbarHostState.showSnackbar("Cultivo creado exitosamente")
                            
                            // Limpiar campos
                            nombre = ""
                            descripcion = ""
                            tipo = ""
                            especie = ""
                            sensorHumedadSuelo = false
                            sensorTemperatura = false
                            sensorHumedadAire = false
                            sensorPh = false
                            sensorConductividad = false
                            sensorNutrientes = false
                            sensorLuz = false
                            
                            // Navegar a lista después de un breve delay
                            kotlinx.coroutines.delay(1000)
                            navController.popBackStack()
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Completa los campos obligatorios (*)")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank() && tipo.isNotBlank() && especie.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Cultivo")
            }
        }
    }
}

// Función auxiliar para mostrar sensores activos (mantenida para compatibilidad)
private fun obtenerSensoresActivos(cultivo: Cultivo): String {
    val sensores = mutableListOf<String>()
    if (cultivo.sensorHumedadSuelo) sensores.add("Humedad")
    if (cultivo.sensorTemperatura) sensores.add("Temp")
    if (cultivo.sensorHumedadAire) sensores.add("H.Aire")
    if (cultivo.sensorLuz) sensores.add("Luz")
    if (cultivo.sensorPh) sensores.add("pH")
    if (cultivo.sensorConductividad) sensores.add("Conductividad")
    if (cultivo.sensorNutrientes) sensores.add("Nutrientes")
    return if (sensores.isEmpty()) "Ninguno" else sensores.joinToString(", ")
}