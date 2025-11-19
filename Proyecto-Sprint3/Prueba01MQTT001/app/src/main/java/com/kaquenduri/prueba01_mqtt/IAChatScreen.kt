// IAChatScreen.kt
package com.kaquenduri.prueba01_mqtt.Views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaquenduri.prueba01_mqtt.ViewModels.SensorViewModel
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.ChatEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IAChatScreen( onBack: () -> Unit = {} ) {
    val viewModel: SensorViewModel = viewModel()
    val mensajes = viewModel.mensajesChat.value
    val cargando = viewModel.cargandoChat.value
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val chatDao = db.chatDao()
    val scope = rememberCoroutineScope()

    // Mantener sólo últimas X horas (por defecto 24h)
    val retentionHours = 24
    val minTime = remember { System.currentTimeMillis() - retentionHours * 60L * 60L * 1000L }
    val historial by chatDao.entriesSince(minTime).collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        chatDao.deleteOlderThan(minTime)
    }

    var textoMensaje by remember { mutableStateOf("") }
    var mostrarSugerencias by remember { mutableStateOf(true) }
    val sugerencias = listOf(
        "¿Cuál es el riego ideal para mi cultivo?",
        "¿Qué plagas comunes debo vigilar?",
        "Recomendaciones de fertilización por etapa",
        "¿Cómo interpretar humedad y temperatura?",
        "Plan de cuidados para esta semana",
        "¿Qué señales indican estrés hídrico?"
    )
    
    // Ocultar sugerencias cuando hay mensajes o cuando la IA está respondiendo
    val deberiaMostrarSugerencias = mostrarSugerencias && mensajes.isEmpty() && !cargando
    
    // Observar cuando la IA termina de responder
    LaunchedEffect(cargando) {
        if (!cargando && mensajes.isNotEmpty()) {
            mostrarSugerencias = false
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Historial (24h)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(historial) { entry ->
                        ListItem(
                            headlineContent = { Text(entry.question) },
                            supportingContent = { entry.answer?.let { Text(it, maxLines = 1) } },
                            leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clickable {
                                    textoMensaje = entry.question
                                    scope.launch { drawerState.close() }
                                }
                        )
                    }
                }
            }
        },
        drawerState = drawerState,
        gesturesEnabled = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = "Asistente de Jardinería IA",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Default.History, contentDescription = "Historial")
                }
            }

            // Sugerencias con animación
            AnimatedVisibility(
                visible = deberiaMostrarSugerencias,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column {
                    Text(
                        text = "Preguntas sugeridas:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sugerencias.forEach { s ->
                            AssistChip(
                                onClick = {
                                    textoMensaje = s
                                    mostrarSugerencias = false
                                },
                                label = { Text(s) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            reverseLayout = true
        ) {
            items(mensajes.reversed()) { mensaje ->
                MensajeChatItem(mensaje = mensaje)
            }
        }

            Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textoMensaje,
                onValueChange = { textoMensaje = it },
                placeholder = { Text("Pregunta sobre tus plantas...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                enabled = !cargando
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textoMensaje.isNotBlank() && !cargando) {
                        val pregunta = textoMensaje
                        mostrarSugerencias = false // Ocultar sugerencias al enviar
                        viewModel.enviarMensajeChat(pregunta)
                        scope.launch {
                            chatDao.insert(
                                ChatEntry(
                                    question = pregunta,
                                    answer = null,
                                    timestampMillis = System.currentTimeMillis()
                                )
                            )
                        }
                        textoMensaje = ""
                    }
                },
                enabled = textoMensaje.isNotBlank() && !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
        }
    }
}

@Composable
fun MensajeChatItem(mensaje: SensorViewModel.MensajeChat) {
    val backgroundColor = if (mensaje.esUsuario) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (mensaje.esUsuario) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (mensaje.esUsuario) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = mensaje.texto,
                color = textColor,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
