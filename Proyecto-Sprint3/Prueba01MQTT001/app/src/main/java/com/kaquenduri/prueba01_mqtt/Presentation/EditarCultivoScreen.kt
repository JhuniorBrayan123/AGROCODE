package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kaquenduri.prueba01_mqtt.ViewModels.CultivoViewModel
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.Cultivo
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarCultivoScreen(
    navController: NavController,
    cultivoId: Int,
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

    // Obtener el cultivo actual
    val cultivoActual by cultivoDao.obtenerCultivoPorId(cultivoId)
        .collectAsState(initial = null)

    // Estados del formulario (inicializados con los datos del cultivo)
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }

    // SENSORES REALES
    var sensorHumedadSuelo by remember { mutableStateOf(false) }
    var sensorTemperatura by remember { mutableStateOf(false) }
    var sensorHumedadAire by remember { mutableStateOf(false) }

    // SENSORES SIMULADOS
    var sensorPh by remember { mutableStateOf(false) }
    var sensorConductividad by remember { mutableStateOf(false) }
    var sensorNutrientes by remember { mutableStateOf(false) }
    var sensorLuz by remember { mutableStateOf(false) }

    // Cargar datos del cultivo cuando esté disponible
    LaunchedEffect(cultivoActual) {
        cultivoActual?.let { cultivo ->
            nombre = cultivo.nombre
            descripcion = cultivo.descripcion
            tipo = cultivo.tipo
            especie = cultivo.especie
            sensorHumedadSuelo = cultivo.sensorHumedadSuelo
            sensorTemperatura = cultivo.sensorTemperatura
            sensorHumedadAire = cultivo.sensorHumedadAire
            sensorPh = cultivo.sensorPh
            sensorConductividad = cultivo.sensorConductividad
            sensorNutrientes = cultivo.sensorNutrientes
            sensorLuz = cultivo.sensorLuz
        }
    }

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
            if (cultivoActual == null) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                Text("Cargando datos del cultivo...")
            } else {
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

                // SENSORES REALES
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9).copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sensores Reales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(16.dp))

                // SENSORES SIMULADOS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD).copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sensores Simulados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de guardar cambios
                Button(
                    onClick = {
                        if (nombre.isNotBlank() && tipo.isNotBlank() && especie.isNotBlank()) {
                            scope.launch {
                                val cultivoActualizado = cultivoActual!!.copy(
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    tipo = tipo,
                                    especie = especie,
                                    sensorHumedadSuelo = sensorHumedadSuelo,
                                    sensorTemperatura = sensorTemperatura,
                                    sensorHumedadAire = sensorHumedadAire,
                                    sensorPh = sensorPh,
                                    sensorConductividad = sensorConductividad,
                                    sensorNutrientes = sensorNutrientes,
                                    sensorLuz = sensorLuz
                                )
                                cultivoViewModel.actualizarCultivo(cultivoActualizado)
                                snackbarHostState.showSnackbar("Cultivo actualizado exitosamente")
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
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios")
                }
            }
        }
    }
}
