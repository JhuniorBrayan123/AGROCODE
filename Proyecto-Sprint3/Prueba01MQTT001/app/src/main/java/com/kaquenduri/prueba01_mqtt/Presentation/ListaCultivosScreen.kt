package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.Cultivo
import com.kaquenduri.prueba01_mqtt.ViewModels.CultivoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaCultivosScreen(
    navController: NavController,
    userId: Int = 1, // Por defecto, luego se puede pasar desde login
    cultivoViewModel: CultivoViewModel = viewModel()
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val cultivoDao = db.cultivoDao()

    // Obtener cultivos del usuario
    val listaCultivos by cultivoDao
        .obtenerTodosCultivosPorUsuario(userId)
        .collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        cultivoViewModel.initRepository(context)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("crearCultivo") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear cultivo")
            }
        }
    ) { padding ->
        if (listaCultivos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🌱",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tienes cultivos aún",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Crea tu primer cultivo para comenzar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("crearCultivo") }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Cultivo")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = padding,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(listaCultivos) { cultivo ->
                    CultivoCard(
                        cultivo = cultivo,
                        onClick = {
                            navController.navigate("dashboardCultivo/${cultivo.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CultivoCard(
    cultivo: Cultivo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "🌱 ${cultivo.nombre}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cultivo.tipo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = cultivo.especie,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sensores activos
            val sensoresActivos = mutableListOf<String>()
            if (cultivo.sensorHumedadSuelo) sensoresActivos.add("💧")
            if (cultivo.sensorTemperatura) sensoresActivos.add("🌡️")
            if (cultivo.sensorHumedadAire) sensoresActivos.add("💨")
            if (cultivo.sensorPh) sensoresActivos.add("pH")
            if (cultivo.sensorConductividad) sensoresActivos.add("⚡")
            if (cultivo.sensorNutrientes) sensoresActivos.add("🧪")
            if (cultivo.sensorLuz) sensoresActivos.add("☀️")

            if (sensoresActivos.isNotEmpty()) {
                Text(
                    text = sensoresActivos.joinToString(" "),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = "Sin sensores",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "📅 ${cultivo.fechaSiembra}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
            )
        }
    }
}

