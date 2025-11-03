package com.example.agrocode.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agrocode.presentation.home.HomeScreen
import com.example.agrocode.presentation.login.LoginScreen
import com.example.agrocode.presentation.registro.RegistroScreen
import com.example.agrocode.presentation.theme.AGROCODETheme
import com.example.agrocode.presentation.theme.WhiteBackground
import com.example.agrocode.presentation.theme.GreenPrimary
import com.example.agrocode.presentation.notifications.Notifier



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Notifier.createChannel(this)
        solicitarPermisoNotificaciones()
        setContent {
            AGROCODETheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    AppNavHost(controladorNavegacion = navController, modificador = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun solicitarPermisoNotificaciones() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val permiso = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permiso), 1001)
            }
        }
    }
}

sealed class Pantalla(val ruta: String) {
    object InicioSesion : Pantalla("inicio_sesion")
    object Registro : Pantalla("registro")
    object Tablero : Pantalla("tablero")
}

@Composable
fun AppNavHost(controladorNavegacion: NavHostController, modificador: Modifier = Modifier) {
    NavHost(navController = controladorNavegacion, startDestination = Pantalla.InicioSesion.ruta, modifier = modificador) {
        composable(Pantalla.InicioSesion.ruta) {
            LoginScreen(
                onLoginExitoso = { controladorNavegacion.navigate(Pantalla.Tablero.ruta) },
                onNavegarARegistro = { controladorNavegacion.navigate(Pantalla.Registro.ruta) }
            )
        }
        composable(Pantalla.Registro.ruta) {
            RegistroScreen(
                onRegistroExitoso = { controladorNavegacion.navigate(Pantalla.Tablero.ruta) },
                onNavegarALogin = { controladorNavegacion.navigate(Pantalla.InicioSesion.ruta) }
            )
        }
        composable(Pantalla.Tablero.ruta) {
            HomeScreen()
        }
    }
}


