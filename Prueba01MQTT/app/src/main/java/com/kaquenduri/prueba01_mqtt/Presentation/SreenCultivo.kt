package com.kaquenduri.prueba01_mqtt.Presentation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.Cultivo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCultivo() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val cultivoDao = db.cultivoDao()
    val scope = rememberCoroutineScope()

    // Estados de formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }

    // Lista reactiva de cultivos desde la BD
    val listaCultivos by cultivoDao
        .obtenerTodosCultivos()
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Cultivos") },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            if (nombre.isNotBlank()) {
                                val fechaActual = SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm",
                                    Locale.getDefault()
                                ).format(Date())

                                val cultivo = Cultivo(
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    tipo = tipo,
                                    especie = especie,
                                    fechaSiembra = "2025-10-26",
                                    idUsuario = 1 // Reemplaza con el ID real del usuario
                                )
                                cultivoDao.insertarCultivo(cultivo)

                                // Limpiar campos
                                nombre = ""
                                descripcion = ""
                                tipo = ""
                                especie = ""
                            }
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar cultivo")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
            TextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
            TextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo") })
            TextField(value = especie, onValueChange = { especie = it }, label = { Text("Especie") })

            Spacer(Modifier.height(16.dp))

            LazyColumn {
                items(listaCultivos) { cultivo ->
                    var editando by remember { mutableStateOf(false) }
                    var nuevoNombre by remember { mutableStateOf(cultivo.nombre) }
                    var nuevaDescripcion by remember { mutableStateOf(cultivo.descripcion) }
                    var nuevoTipo by remember { mutableStateOf(cultivo.tipo) }
                    var nuevaEspecie by remember { mutableStateOf(cultivo.especie) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            if (editando) {
                                // 🔧 Campos de edición
                                TextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre") })
                                TextField(value = nuevaDescripcion, onValueChange = { nuevaDescripcion = it }, label = { Text("Descripción") })
                                TextField(value = nuevoTipo, onValueChange = { nuevoTipo = it }, label = { Text("Tipo") })
                                TextField(value = nuevaEspecie, onValueChange = { nuevaEspecie = it }, label = { Text("Especie") })

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Button(onClick = {
                                        scope.launch {
                                            cultivoDao.actualizarCultivo(
                                                cultivo.copy(
                                                    nombre = nuevoNombre,
                                                    descripcion = nuevaDescripcion,
                                                    tipo = nuevoTipo,
                                                    especie = nuevaEspecie
                                                )
                                            )
                                            editando = false
                                        }
                                    }) {
                                        Text("Guardar")
                                    }
                                    OutlinedButton(onClick = { editando = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            } else {
                                // 👀 Vista normal
                                Text("🌱 ${cultivo.nombre}", style = MaterialTheme.typography.titleMedium)
                                Text("Tipo: ${cultivo.tipo}")
                                Text("Especie: ${cultivo.especie}")
                                Text("📅 ${cultivo.fechaSiembra}", style = MaterialTheme.typography.bodySmall)

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(onClick = { editando = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar cultivo")
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            cultivoDao.eliminarCultivoPorId(cultivo.id)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar cultivo")
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}
