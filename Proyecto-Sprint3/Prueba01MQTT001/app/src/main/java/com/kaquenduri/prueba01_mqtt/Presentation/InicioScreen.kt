package com.kaquenduri.prueba01_mqtt.Presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kaquenduri.prueba01_mqtt.ViewModels.InicioViewModel

@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    navController: NavHostController
){
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 6 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Text(
            text = "¡Bienvenido/a a AgroCode!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tu asistente para gestionar cultivos y sensores.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider()

        Text(
            text = "¿Qué puedes hacer aquí?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("- Ver y gestionar tus cultivos", fontSize = 16.sp)
                Text("- Crear nuevos cultivos", fontSize = 16.sp)
                Text("- Monitorear sensores y gráficos", fontSize = 16.sp)
                Text("- Registrar actividades y gastos", fontSize = 16.sp)
                Text("- Recibir alertas y recomendaciones", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.navigate("listaCultivos") }, modifier = Modifier.fillMaxWidth()) {
                Text("Ir a Mis Cultivos")
            }
            OutlinedButton(onClick = { navController.navigate("crearCultivo") }, modifier = Modifier.fillMaxWidth()) {
                Text("Crear un Cultivo Nuevo")
            }
            OutlinedButton(onClick = { navController.navigate("ia_chat") }, modifier = Modifier.fillMaxWidth()) {
                Text("Abrir Asistente (IA)")
            }
        }
        }
    }
}