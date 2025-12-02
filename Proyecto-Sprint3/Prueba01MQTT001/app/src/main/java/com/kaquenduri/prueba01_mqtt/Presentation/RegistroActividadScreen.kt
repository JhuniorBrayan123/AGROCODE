// Presentation/RegistroActividadScreen.kt
package com.kaquenduri.prueba01_mqtt.Presentation

import android.util.Log
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
import androidx.compose.material3.HorizontalDivider
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
    val firestore = FirebaseFirestore.getInstance()

    // Estados del formulario
    var fecha by remember { mutableStateOf("") }
    var tipoActividad by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var jornales by remember { mutableStateOf("") }
    var materiales by remember { mutableStateOf("") }
    var actividadesFirebase by remember { mutableStateOf<List<RegistroActividad>>(emptyList()) }

    // Estado para mostrar confirmación
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }



    // Lista de actividades
    val listaActividades by actividadDao
        .obtenerActividadesPorCultivo(cultivoId)
        .collectAsState(initial = emptyList())

    //Funcion para filtrar las actividades por cultivo y devolverlas
    fun cargarActividadesFirebase() {
        scope.launch {
            try {
                val snapshot = firestore.collection("Actividades")
                    .whereEqualTo("cultivoId", cultivoId)
                    .get()
                    .await()

                val lista = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    RegistroActividad(
                        id = 0,
                        cultivoId = (data["cultivoId"] as? Number)?.toInt() ?: 0,

                        fecha = data["fecha"] as? String ?: "",
                        tipoActividad = data["tipoActividad"] as? String ?: "",
                        descripcion = data["descripcion"] as? String ?: "",
                        productos = data["productosAplicados"] as? String ?: "",
                        costo = (data["costo"] as? Number)?.toDouble() ?: 0.0,
                        jornales = (data["jornales"] as? Number)?.toInt() ?: 0,
                        materiales = data["materialesUtilizados"] as? String ?: ""
                    )
                }

                actividadesFirebase = lista

                Log.d("FIREBASE_ACTIVIDADES", "Total actividades cargadas: ${lista.size}")
                Log.d("FIREBASE_ACTIVIDADES", "Contenido: $lista")

            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al cargar actividades: ${e.message}")
            }
        }
    }

    //Cargar actividades
    LaunchedEffect(Unit) {
        cargarActividadesFirebase()

    }


    // Función para guardar en Firestore y Room
    fun guardarActividad() {
        if (fecha.isNotBlank() && tipoActividad.isNotBlank()) {
            guardando = true
            scope.launch {
                try {
                    val actividad = RegistroActividad(
                        cultivoId = cultivoId,
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
                    cargarActividadesFirebase()
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
    @Composable
    fun ModernInput(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        maxLines: Int = 1
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = modifier.fillMaxWidth(),
            maxLines = maxLines,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }

    @Composable
    fun ActivityInfoRow(actividad: RegistroActividad) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (actividad.productos.isNotBlank()) {
                Column {
                    Text("🧪 Productos", style = MaterialTheme.typography.labelSmall)
                    Text(actividad.productos, fontWeight = FontWeight.Medium)
                }
            }
            if (actividad.costo > 0) {
                Column {
                    Text("💰 Costo", style = MaterialTheme.typography.labelSmall)
                    Text("$${actividad.costo}", fontWeight = FontWeight.Medium)
                }
            }
            if (actividad.jornales > 0) {
                Column {
                    Text("👥 Jornales", style = MaterialTheme.typography.labelSmall)
                    Text("${actividad.jornales}", fontWeight = FontWeight.Medium)
                }
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // FORMULARIO
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        // Encabezado del formulario
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Nueva Actividad",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            AnimatedVisibility(
                                visible = mostrarConfirmacion,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        "Guardado",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // CAMPOS
                        ModernInput(
                            value = fecha,
                            onValueChange = { fecha = it },
                            label = "Fecha (dd/mm/aaaa) *"
                        )
                        ModernInput(
                            value = tipoActividad,
                            onValueChange = { tipoActividad = it },
                            label = "Tipo de Actividad *"
                        )
                        ModernInput(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = "Descripción",
                            maxLines = 4
                        )
                        ModernInput(
                            value = productos,
                            onValueChange = { productos = it },
                            label = "Productos aplicados"
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            ModernInput(
                                value = costo,
                                onValueChange = { costo = it },
                                label = "Costo",
                                modifier = Modifier.weight(1f)
                            )
                            ModernInput(
                                value = jornales,
                                onValueChange = { jornales = it },
                                label = "Jornales",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        ModernInput(
                            value = materiales,
                            onValueChange = { materiales = it },
                            label = "Materiales utilizados"
                        )

                        Button(
                            onClick = { guardarActividad() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !guardando && fecha.isNotBlank() && tipoActividad.isNotBlank(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Guardar Actividad", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (actividadesFirebase.isNotEmpty()) {
                    Text(
                        text = "Actividades Registradas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 10.dp)
                    )
                }
            }

            // LISTA DE ACTIVIDADES
            items(
                items = actividadesFirebase,
                key = { it.hashCode() }
            ) { actividad ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "📅 ${actividad.fecha}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    "🔧 ${actividad.tipoActividad}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            
                        }

                        actividad.descripcion.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        ActivityInfoRow(actividad)
                    }
                }
            }
        }


    }
}
