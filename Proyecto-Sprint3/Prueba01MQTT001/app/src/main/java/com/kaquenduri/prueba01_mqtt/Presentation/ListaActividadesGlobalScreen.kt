// Presentation/ListaActividadesGlobalScreen.kt
package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.RegistroActividad
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaActividadesGlobalScreen(
    navController: NavController,
    userId: Int = 1
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val actividadDao = db.registroActividadDao()
    val cultivoDao = db.cultivoDao()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para filtros
    var mostrarFiltros by remember { mutableStateOf(false) }
    var filtroTipo by remember { mutableStateOf("") }

    // Obtener todas las actividades del usuario
    val todasActividades by actividadDao
        .obtenerTodasActividadesPorUsuario(userId)
        .collectAsState(initial = emptyList())

    // Obtener todos los cultivos para mostrar nombres
    val todosCultivos by cultivoDao
        .obtenerTodosCultivosPorUsuario(userId)
        .collectAsState(initial = emptyList())

    // Filtrar actividades
    val actividadesFiltradas = remember(todasActividades, filtroTipo) {
        if (filtroTipo.isBlank()) {
            todasActividades
        } else {
            todasActividades.filter { it.tipoActividad.contains(filtroTipo, ignoreCase = true) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarFiltros = !mostrarFiltros },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtros")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header con estadísticas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Resumen de Actividades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${todasActividades.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Total actividades",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column {
                            Text(
                                text = "${todosCultivos.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Cultivos activos",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column {
                            val costoTotal = todasActividades.sumOf { it.costo }
                            Text(
                                text = "$${"%.2f".format(costoTotal)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Costo total",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Filtros (desplegable)
            if (mostrarFiltros) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = filtroTipo,
                            onValueChange = { filtroTipo = it },
                            label = { Text("Filtrar por tipo de actividad") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { filtroTipo = "" },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Limpiar filtros")
                        }
                    }
                }
            }

            // Lista de actividades
            if (actividadesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📝",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (filtroTipo.isBlank()) 
                                "No hay actividades registradas" 
                            else 
                                "No se encontraron actividades",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = actividadesFiltradas,
                        key = { it.id }
                    ) { actividad ->
                        // Obtener nombre del cultivo
                        val nombreCultivo = remember(actividad.cultivoId, todosCultivos) {
                            todosCultivos.find { it.id == actividad.cultivoId }?.nombre ?: "Cultivo #${actividad.cultivoId}"
                        }

                        ActividadGlobalCard(
                            actividad = actividad,
                            nombreCultivo = nombreCultivo,
                            onDelete = {
                                scope.launch {
                                    try {
                                        actividadDao.eliminarActividadPorId(actividad.id)
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
                            },
                            onNavigateToCultivo = {
                                navController.navigate("dashboardCultivo/${actividad.cultivoId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActividadGlobalCard(
    actividad: RegistroActividad,
    nombreCultivo: String,
    onDelete: () -> Unit,
    onNavigateToCultivo: () -> Unit
) {
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

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
            // Header con cultivo y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Nombre del cultivo (clickeable)
                    TextButton(
                        onClick = onNavigateToCultivo,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "🌱 $nombreCultivo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "📅 ${actividad.fecha}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🔧 ${actividad.tipoActividad}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(
                    onClick = { mostrarDialogoEliminar = true }
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            // Detalles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (actividad.productos.isNotBlank()) {
                    DetailChip("🧪", actividad.productos)
                }

                if (actividad.costo > 0) {
                    DetailChip("💰", "$${actividad.costo}")
                }

                if (actividad.jornales > 0) {
                    DetailChip("👥", "${actividad.jornales}")
                }
            }

            if (actividad.materiales.isNotBlank()) {
                DetailChip("🔨", actividad.materiales)
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("¿Eliminar actividad?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        mostrarDialogoEliminar = false
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

@Composable
fun DetailChip(icon: String, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon)
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
