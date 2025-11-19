// Presentation/RegistroActividadScreen.kt
package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.RegistroActividad
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroActividadScreen(navController: NavController, cultivoId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val actividadDao = db.registroActividadDao()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    // Estados del formulario
    var fecha by remember { mutableStateOf("") }
    var tipoActividad by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var jornales by remember { mutableStateOf("") }
    var materiales by remember { mutableStateOf("") }
    
    // Estado para mostrar confirmación
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    // Lista de actividades
    val listaActividades by actividadDao
        .obtenerActividadesPorCultivo(cultivoId)
        .collectAsState(initial = emptyList())

    // Función para guardar en Firestore y Room
    fun guardarActividad() {
        if (fecha.isNotBlank() && tipoActividad.isNotBlank()) {
            guardando = true
            scope.launch {
                try {
                    val actividad = RegistroActividad(
                        cultivoId = cultivoId,
                        idUsuario = 1,
                        fecha = fecha,
                        tipoActividad = tipoActividad,
                        descripcion = descripcion,
                        productos = productos,
                        costo = costo.toDoubleOrNull() ?: 0.0,
                        jornales = jornales.toIntOrNull() ?: 0,
                        materiales = materiales
                    )
                    
                    // Guardar en Room primero
                    actividadDao.insertarActividad(actividad)
                    
                    // Guardar en Firestore
                    val actividadData = hashMapOf(
                        "cultivoId" to cultivoId,
                        "idUsuario" to 1,
                        "fecha" to fecha,
                        "tipoActividad" to tipoActividad,
                        "descripcion" to descripcion,
                        "productosAplicados" to productos,
                        "costo" to (costo.toDoubleOrNull() ?: 0.0),
                        "jornales" to (jornales.toIntOrNull() ?: 0),
                        "materialesUtilizados" to materiales,
                        "timestamp" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("Actividades")
                        .add(actividadData)
                        .await()
                    
                    // Limpiar campos
                    fecha = ""
                    tipoActividad = ""
                    descripcion = ""
                    productos = ""
                    costo = ""
                    jornales = ""
                    materiales = ""
                    
                    mostrarConfirmacion = true
                    snackbarHostState.showSnackbar(
                        "Actividad guardada exitosamente",
                        duration = SnackbarDuration.Short
                    )
                    
                    // Ocultar confirmación después de 2 segundos
                    kotlinx.coroutines.delay(2000)
                    mostrarConfirmacion = false
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(
                        "Error al guardar: ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                } finally {
                    guardando = false
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Completa los campos obligatorios (Fecha y Tipo de Actividad)",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { guardarActividad() },
                containerColor = if (guardando || fecha.isBlank() || tipoActividad.isBlank())
                    Color.Gray
                else
                    MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Guardar actividad"
                    )
                }
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Tarjeta del formulario con animación
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    // Título del formulario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Nueva Actividad",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Indicador de confirmación
                        AnimatedVisibility(
                            visible = mostrarConfirmacion,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Guardado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Campos del formulario con espaciado mejorado
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha (dd/mm/aaaa) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = tipoActividad,
                        onValueChange = { tipoActividad = it },
                        label = { Text("Tipo de Actividad *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = productos,
                        onValueChange = { productos = it },
                        label = { Text("Productos aplicados") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = costo,
                            onValueChange = { costo = it },
                            label = { Text("Costo") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = jornales,
                            onValueChange = { jornales = it },
                            label = { Text("Jornales") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    OutlinedTextField(
                        value = materiales,
                        onValueChange = { materiales = it },
                        label = { Text("Materiales utilizados") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    // Botón de guardar
                    Button(
                        onClick = { guardarActividad() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardando && (fecha.isNotBlank() && tipoActividad.isNotBlank()),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Guardar Actividad")
                    }
                }
            }

            // Lista de actividades existentes con mejor diseño
            if (listaActividades.isNotEmpty()) {
                Text(
                    text = "Actividades Registradas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = listaActividades,
                    key = { it.id }
                ) { actividad ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📅 ${actividad.fecha}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "🔧 ${actividad.tipoActividad}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                // Eliminar de Room
                                                actividadDao.eliminarActividadPorId(actividad.id)
                                                
                                                // Eliminar de Firestore (opcional, si tienes el ID del documento)
                                                snackbarHostState.showSnackbar(
                                                    "Actividad eliminada",
                                                    duration = SnackbarDuration.Short
                                                )
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar(
                                                    "Error al eliminar: ${e.message}",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            if (actividad.descripcion.isNotBlank()) {
                                Text(
                                    text = actividad.descripcion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (actividad.productos.isNotBlank() || actividad.costo > 0 || actividad.jornales > 0) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (actividad.productos.isNotBlank()) {
                                    Column {
                                        Text(
                                            text = "🧪 Productos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = actividad.productos,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (actividad.costo > 0) {
                                    Column {
                                        Text(
                                            text = "💰 Costo",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$${actividad.costo}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (actividad.jornales > 0) {
                                    Column {
                                        Text(
                                            text = "👥 Jornales",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${actividad.jornales}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            if (actividad.materiales.isNotBlank()) {
                                Column {
                                    Text(
                                        text = "🔨 Materiales",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = actividad.materiales,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
