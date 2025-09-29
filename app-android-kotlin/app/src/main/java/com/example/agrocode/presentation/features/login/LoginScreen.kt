package com.example.agrocode.presentation.features.login


import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrocode.R
import com.example.agrocode.presentation.theme.GreenPrimary


@Composable
fun LoginScreen(viewModel: LoginViewModel){
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)

    ){
      Login(Modifier.align(Alignment.Center), viewModel )
    }
}

@Composable
fun Login(modifier: Modifier, viewModel: LoginViewModel){

    Column (modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        //Logotipo
        HeaderLogin()

        //Espacio entre imagen y texto
        Spacer(modifier = Modifier.padding(16.dp))

        //Login formulario
        FormularioLogin(viewModel)

        //Espacio entre imagen y texto
        Spacer(modifier = Modifier.padding(8.dp))

        ContraseñaOlvidada(Modifier.align(Alignment.End))

        Spacer(modifier = Modifier.padding(16.dp))

        BotonLogin()
    }
}

@Composable
fun BotonLogin() {
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF87B93F),
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White
        )

    ) {
        Text("Iniciar Sesión")
    }
}


//---Componentes de Pantalla---
@Composable
fun HeaderLogin(){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        //Imagen logotipo
        Image(painter = painterResource(R.drawable.applogo2), contentDescription = "Logo de app",
            modifier = Modifier.size(200.dp))

        Text("Maneja tus cultivos", fontSize = 24.sp, color = Color(0xFF85B93D))
    }
}

@Composable
fun FormularioLogin(viewModel: LoginViewModel){

    val email : String by viewModel.email.observeAsState(initial = "")

    Column {
        // Campo de correo
        OutlinedTextField(
            value = " ",
            onValueChange = {},
            label = { Text("Correo electrónico") },
            placeholder = { Text("ejemplo@correo.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Correo",
                    tint = Color(0xFF87B93F)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF87B93F), // Cambiar a interfaces
                focusedLabelColor = Color(0xFF87B93F),
                cursorColor = Color(0xFF87B93F)
            ),
        )

        Spacer(modifier = Modifier.padding(16.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = " ",
            onValueChange = {},
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Contraseña",
                    tint = Color(0xFF87B93F)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF87B93F),
                focusedLabelColor = Color(0xFF87B93F),
                cursorColor = Color(0xFF87B93F)
            ),
        )


    }


}

@Composable
fun ContraseñaOlvidada(modifier : Modifier) {
    Text(
        text = "¿Olvidaste tu contraseña?",
        fontSize = 14.sp,color = Color(0xFF85B93D),
        modifier = modifier.clickable {},
        fontWeight = FontWeight.Bold
    )
}



@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview(){
    LoginScreen(LoginViewModel())
}

