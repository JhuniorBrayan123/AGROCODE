// IAChatScreen.kt
package com.kaquenduri.prueba01_mqtt.Views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaquenduri.prueba01_mqtt.ViewModels.SensorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IAChatScreen() {
    val viewModel: SensorViewModel = viewModel()
    val mensajes = viewModel.mensajesChat.value
    val cargando = viewModel.cargandoChat.value


    var textoMensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Asistente de Jardinería IA",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                        viewModel.enviarMensajeChat(textoMensaje)
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
