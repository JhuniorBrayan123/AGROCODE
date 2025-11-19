package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionSonoraScreen(
    irHome: () -> Unit
) {
    // Estados para configuración sonora
    var volumenAlerta by remember { mutableStateOf(0.7f) }
    var tipoAlerta by remember { mutableStateOf("Notificación") }
    var alertasSonorasActivas by remember { mutableStateOf(true) }
    var vibracionActiva by remember { mutableStateOf(true) }
    var tonoPersonalizado by remember { mutableStateOf(false) }
    
    val tiposAlerta = listOf("Notificación", "Alarma", "Sonido Suave", "Personalizado")
    
    Scaffold(
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuración General
            item {
                ConfiguracionGeneralCard(
                    alertasSonorasActivas = alertasSonorasActivas,
                    onAlertasSonorasChange = { alertasSonorasActivas = it },
                    vibracionActiva = vibracionActiva,
                    onVibracionChange = { vibracionActiva = it }
                )
            }
            
            // Configuración de Volumen
            item {
                ConfiguracionVolumenCard(
                    volumen = volumenAlerta,
                    onVolumenChange = { volumenAlerta = it }
                )
            }
            
            // Configuración de Tipo de Alerta
            item {
                ConfiguracionTipoAlertaCard(
                    tipoAlerta = tipoAlerta,
                    tiposDisponibles = tiposAlerta,
                    onTipoChange = { tipoAlerta = it },
                    tonoPersonalizado = tonoPersonalizado,
                    onTonoPersonalizadoChange = { tonoPersonalizado = it }
                )
            }
            
            // Vista Previa
            item {
                VistaPreviaCard(
                    volumen = volumenAlerta,
                    tipoAlerta = tipoAlerta,
                    alertasActivas = alertasSonorasActivas
                )
            }
            
            // Configuraciones por Tipo de Alerta
            item {
                ConfiguracionPorTipoCard(
                    tipoAlerta = tipoAlerta,
                    volumen = volumenAlerta
                )
            }
        }
    }
}

@Composable
fun ConfiguracionGeneralCard(
    alertasSonorasActivas: Boolean,
    onAlertasSonorasChange: (Boolean) -> Unit,
    vibracionActiva: Boolean,
    onVibracionChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Configuración General",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (alertasSonorasActivas) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = null,
                        tint = if (alertasSonorasActivas) Color(0xFF2E7D32) else Color(0xFF757575)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Alertas Sonoras")
                }
                Switch(
                    checked = alertasSonorasActivas,
                    onCheckedChange = onAlertasSonorasChange
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibración")
                Switch(
                    checked = vibracionActiva,
                    onCheckedChange = onVibracionChange
                )
            }
        }
    }
}

@Composable
fun ConfiguracionVolumenCard(
    volumen: Float,
    onVolumenChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Volumen de Alerta",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Text("Volumen: ${(volumen * 100).roundToInt()}%")
            Slider(
                value = volumen,
                onValueChange = onVolumenChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF2E7D32),
                    activeTrackColor = Color(0xFF2E7D32)
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Silencioso", style = MaterialTheme.typography.bodySmall)
                Text("Máximo", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ConfiguracionTipoAlertaCard(
    tipoAlerta: String,
    tiposDisponibles: List<String>,
    onTipoChange: (String) -> Unit,
    tonoPersonalizado: Boolean,
    onTonoPersonalizadoChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tipo de Alerta",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Box {
                OutlinedTextField(
                    value = tipoAlerta,
                    onValueChange = {},
                    label = { Text("Seleccionar tipo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    tiposDisponibles.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                onTipoChange(tipo)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            if (tipoAlerta == "Personalizado") {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Usar tono personalizado")
                    Switch(
                        checked = tonoPersonalizado,
                        onCheckedChange = onTonoPersonalizadoChange
                    )
                }
            }
        }
    }
}

@Composable
fun VistaPreviaCard(
    volumen: Float,
    tipoAlerta: String,
    alertasActivas: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vista Previa",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estado:")
                Text(
                    text = if (alertasActivas) "Activo" else "Inactivo",
                    color = if (alertasActivas) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tipo:")
                Text(tipoAlerta, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Volumen:")
                Text("${(volumen * 100).roundToInt()}%", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = { /* Reproducir sonido de prueba */ },
                enabled = alertasActivas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar Alerta")
            }
        }
    }
}

@Composable
fun ConfiguracionPorTipoCard(
    tipoAlerta: String,
    volumen: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Configuración por Tipo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            val configuracion = when (tipoAlerta) {
                "Notificación" -> "Sonido suave y corto, ideal para alertas discretas"
                "Alarma" -> "Sonido fuerte y repetitivo, para alertas críticas"
                "Sonido Suave" -> "Tono melodioso y relajante, para recordatorios"
                "Personalizado" -> "Configuración personalizada según tus preferencias"
                else -> "Configuración estándar del sistema"
            }
            
            Text(
                text = configuracion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(12.dp))
            
            val recomendacion = when (tipoAlerta) {
                "Notificación" -> "Recomendado para uso diario"
                "Alarma" -> "Recomendado para alertas críticas de humedad"
                "Sonido Suave" -> "Recomendado para recordatorios suaves"
                "Personalizado" -> "Configura según tus necesidades específicas"
                else -> "Configuración por defecto"
            }
            
            Text(
                text = "💡 $recomendacion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
