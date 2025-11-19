package com.kaquenduri.prueba01_mqtt.Presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaquenduri.prueba01_mqtt.ViewModels.RegistroViewModel

@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel = viewModel(),
    RegistroExistoso: () -> Unit,
    IrLogin: () -> Unit
) {
    val context = LocalContext.current

    // Inicializamos el repositorio
    LaunchedEffect(Unit) {
        viewModel.initRepository(context)
    }

    // Escucha cambios en el mensaje
    val mensaje = viewModel.mensaje.value

    // Si el mensaje indica éxito, mostramos Toast y navegamos
    LaunchedEffect(mensaje) {
        if (mensaje == "Registro exitoso.") {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            RegistroExistoso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registro de Usuario", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.email.value,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.password.value,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.confirm.value,
            onValueChange = { viewModel.onConfirmChanged(it) },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.registrarUsuario {
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    RegistroExistoso()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = if (mensaje == "Registro exitoso.") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = IrLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
