package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionLimitesScreen(
    viewModel: SensorViewModel,
    irHome: () -> Unit
) {
    // Estados para los límites personalizados
    var limiteHumedadAlta by remember { mutableStateOf(80f) }
    var limiteHumedadBaja by remember { mutableStateOf(20f) }
    var limiteTemperaturaAlta by remember { mutableStateOf(35f) }
    var limiteTemperaturaBaja by remember { mutableStateOf(10f) }
    var limiteHumedadAireAlta by remember { mutableStateOf(85f) }
    var limiteHumedadAireBaja by remember { mutableStateOf(25f) }
    
    // Estados para configuraciones guardadas
    var nombreConfiguracion by remember { mutableStateOf("") }
    var configuracionesGuardadas by remember { mutableStateOf(listOf<ConfiguracionLimites>()) }
    
    Scaffold(
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuración de Límites de Humedad
            item {
                LimitesCard(
                    titulo = "Límites de Humedad del Suelo",
                    limiteAlto = limiteHumedadAlta,
                    limiteBajo = limiteHumedadBaja,
                    onLimiteAltoChange = { limiteHumedadAlta = it },
                    onLimiteBajoChange = { limiteHumedadBaja = it },
                    unidad = "%",
                    color = Color(0xFF2E7D32)
                )
            }
            
            // Configuración de Límites de Temperatura
            item {
                LimitesCard(
                    titulo = "Límites de Temperatura",
                    limiteAlto = limiteTemperaturaAlta,
                    limiteBajo = limiteTemperaturaBaja,
                    onLimiteAltoChange = { limiteTemperaturaAlta = it },
                    onLimiteBajoChange = { limiteTemperaturaBaja = it },
                    unidad = "°C",
                    color = Color(0xFFD32F2F)
                )
            }
            
            // Configuración de Límites de Humedad del Aire
            item {
                LimitesCard(
                    titulo = "Límites de Humedad del Aire",
                    limiteAlto = limiteHumedadAireAlta,
                    limiteBajo = limiteHumedadAireBaja,
                    onLimiteAltoChange = { limiteHumedadAireAlta = it },
                    onLimiteBajoChange = { limiteHumedadAireBaja = it },
                    unidad = "%",
                    color = Color(0xFF1976D2)
                )
            }
            
            // Guardar Configuración
            item {
                GuardarConfiguracionCard(
                    nombreConfiguracion = nombreConfiguracion,
                    onNombreChange = { nombreConfiguracion = it },
                    onGuardar = {
                        val nuevaConfig = ConfiguracionLimites(
                            nombre = nombreConfiguracion,
                            limiteHumedadAlta = limiteHumedadAlta,
                            limiteHumedadBaja = limiteHumedadBaja,
                            limiteTemperaturaAlta = limiteTemperaturaAlta,
                            limiteTemperaturaBaja = limiteTemperaturaBaja,
                            limiteHumedadAireAlta = limiteHumedadAireAlta,
                            limiteHumedadAireBaja = limiteHumedadAireBaja
                        )
                        configuracionesGuardadas = configuracionesGuardadas + nuevaConfig
                        nombreConfiguracion = ""
                    }
                )
            }
            
            // Configuraciones Guardadas
            item {
                ConfiguracionesGuardadasCard(
                    configuraciones = configuracionesGuardadas,
                    onCargarConfiguracion = { config ->
                        limiteHumedadAlta = config.limiteHumedadAlta
                        limiteHumedadBaja = config.limiteHumedadBaja
                        limiteTemperaturaAlta = config.limiteTemperaturaAlta
                        limiteTemperaturaBaja = config.limiteTemperaturaBaja
                        limiteHumedadAireAlta = config.limiteHumedadAireAlta
                        limiteHumedadAireBaja = config.limiteHumedadAireBaja
                    },
                    onEliminarConfiguracion = { config ->
                        configuracionesGuardadas = configuracionesGuardadas.filter { it != config }
                    }
                )
            }
            
            // Recomendaciones de Valores
            item {
                RecomendacionesValoresCard()
            }
        }
    }
}

@Composable
fun LimitesCard(
    titulo: String,
    limiteAlto: Float,
    limiteBajo: Float,
    onLimiteAltoChange: (Float) -> Unit,
    onLimiteBajoChange: (Float) -> Unit,
    unidad: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            
            // Límite Alto
            Text("Límite Alto: ${limiteAlto.roundToInt()}$unidad")
            Slider(
                value = limiteAlto,
                onValueChange = onLimiteAltoChange,
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Límite Bajo
            Text("Límite Bajo: ${limiteBajo.roundToInt()}$unidad")
            Slider(
                value = limiteBajo,
                onValueChange = onLimiteBajoChange,
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
            )
            
            // Validación
            if (limiteBajo >= limiteAlto) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "⚠ El límite bajo debe ser menor al límite alto",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun GuardarConfiguracionCard(
    nombreConfiguracion: String,
    onNombreChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Guardar Configuración",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = nombreConfiguracion,
                onValueChange = onNombreChange,
                label = { Text("Nombre de la configuración") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = onGuardar,
                enabled = nombreConfiguracion.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar Configuración")
            }
        }
    }
}

@Composable
fun ConfiguracionesGuardadasCard(
    configuraciones: List<ConfiguracionLimites>,
    onCargarConfiguracion: (ConfiguracionLimites) -> Unit,
    onEliminarConfiguracion: (ConfiguracionLimites) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Configuraciones Guardadas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            if (configuraciones.isEmpty()) {
                Text(
                    text = "No hay configuraciones guardadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                configuraciones.forEach { config ->
                    ConfiguracionItem(
                        configuracion = config,
                        onCargar = { onCargarConfiguracion(config) },
                        onEliminar = { onEliminarConfiguracion(config) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ConfiguracionItem(
    configuracion: ConfiguracionLimites,
    onCargar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = configuracion.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "H: ${configuracion.limiteHumedadBaja.roundToInt()}-${configuracion.limiteHumedadAlta.roundToInt()}% | " +
                            "T: ${configuracion.limiteTemperaturaBaja.roundToInt()}-${configuracion.limiteTemperaturaAlta.roundToInt()}°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                Button(
                    onClick = onCargar,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Cargar")
                }
                OutlinedButton(onClick = onEliminar) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun RecomendacionesValoresCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Valores Recomendados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            val recomendaciones = listOf(
                "Humedad del Suelo: 30-60% (óptimo: 40-50%)",
                "Temperatura: 18-28°C (óptimo: 22-25°C)",
                "Humedad del Aire: 40-70% (óptimo: 50-60%)"
            )
            
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

data class ConfiguracionLimites(
    val nombre: String,
    val limiteHumedadAlta: Float,
    val limiteHumedadBaja: Float,
    val limiteTemperaturaAlta: Float,
    val limiteTemperaturaBaja: Float,
    val limiteHumedadAireAlta: Float,
    val limiteHumedadAireBaja: Float
)
