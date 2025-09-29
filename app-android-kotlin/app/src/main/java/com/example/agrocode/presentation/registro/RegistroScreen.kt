package com.example.agrocode.presentation.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrocode.presentation.theme.AGROCODETheme
import com.example.agrocode.presentation.theme.GreenPrimary
import com.example.agrocode.presentation.theme.WhiteBackground

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit = {},
    onNavegarALogin: () -> Unit = {},
    viewModel: RegistroScreenViewModel = viewModel()
) {
    val estadoUI by viewModel.estadoUI.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmarContrasena by remember { mutableStateOf(false) }
    
    // Manejar registro exitoso
    LaunchedEffect(estadoUI.registroExitoso) {
        if (estadoUI.registroExitoso) {
            onRegistroExitoso()
        }
    }
    
    // Mostrar errores
    LaunchedEffect(estadoUI.mensajeError) {
        estadoUI.mensajeError?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GreenPrimary.copy(alpha = 0.1f),
                            WhiteBackground
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                // Logo y título
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌱",
                            fontSize = 40.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Crear Cuenta",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                
                Text(
                    text = "Únete a la experiencia de cuidar tu propio cultivo.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Formulario de registro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WhiteBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        
                        // Campos de nombre y apellido
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = estadoUI.nombre,
                                onValueChange = viewModel::actualizarNombre,
                                label = { Text("Nombre") },
                                placeholder = { Text("Tu nombre") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Nombre",
                                        tint = GreenPrimary
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    focusedLabelColor = GreenPrimary,
                                    cursorColor = GreenPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            OutlinedTextField(
                                value = estadoUI.apellido,
                                onValueChange = viewModel::actualizarApellido,
                                label = { Text("Apellido") },
                                placeholder = { Text("Tu apellido") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    focusedLabelColor = GreenPrimary,
                                    cursorColor = GreenPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        
                        // Campo de correo
                        OutlinedTextField(
                            value = estadoUI.correo,
                            onValueChange = viewModel::actualizarCorreo,
                            label = { Text("Correo electrónico") },
                            placeholder = { Text("ejemplo@correo.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Correo",
                                    tint = GreenPrimary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                                cursorColor = GreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        // Campo de teléfono
                        OutlinedTextField(
                            value = estadoUI.telefono,
                            onValueChange = viewModel::actualizarTelefono,
                            label = { Text("Teléfono (opcional)") },
                            placeholder = { Text("+1234567890") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Teléfono",
                                    tint = GreenPrimary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                                cursorColor = GreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        // Campo de contraseña
                        OutlinedTextField(
                            value = estadoUI.contrasena,
                            onValueChange = viewModel::actualizarContrasena,
                            label = { Text("Contraseña") },
                            placeholder = { Text("Mínimo 6 caracteres") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Contraseña",
                                    tint = GreenPrimary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                                    Icon(
                                        imageVector = if (mostrarContrasena) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = GreenPrimary
                                    )
                                }
                            },
                            visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                                cursorColor = GreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        // Campo de confirmar contraseña
                        OutlinedTextField(
                            value = estadoUI.confirmarContrasena,
                            onValueChange = viewModel::actualizarConfirmarContrasena,
                            label = { Text("Confirmar contraseña") },
                            placeholder = { Text("Repite tu contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Confirmar contraseña",
                                    tint = GreenPrimary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { mostrarConfirmarContrasena = !mostrarConfirmarContrasena }) {
                                    Icon(
                                        imageVector = if (mostrarConfirmarContrasena) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (mostrarConfirmarContrasena) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = GreenPrimary
                                    )
                                }
                            },
                            visualTransformation = if (mostrarConfirmarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                                cursorColor = GreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Botón de crear cuenta
                        Button(
                            onClick = viewModel::crearCuenta,
                            enabled = !estadoUI.cargando && 
                                    estadoUI.correo.isNotEmpty() && 
                                    estadoUI.contrasena.isNotEmpty() &&
                                    estadoUI.nombre.isNotEmpty() &&
                                    estadoUI.apellido.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                disabledContainerColor = GreenPrimary.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (estadoUI.cargando) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Crear Cuenta",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Enlace para iniciar sesión
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¿Ya tienes cuenta? ",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            TextButton(
                                onClick = onNavegarALogin,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = GreenPrimary
                                )
                            ) {
                                Text(
                                    text = "Iniciar sesión",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Información adicional
                Text(
                    text = "Comienza tu jornada agrícola inteligente",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroScreenPreview() {
    AGROCODETheme {
        RegistroScreen()
    }
}
