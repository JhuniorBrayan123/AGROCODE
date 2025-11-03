package com.kaquenduri.prueba01_mqtt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.kaquenduri.prueba01_mqtt.Presentation.*
import com.kaquenduri.prueba01_mqtt.ViewModels.*
import com.kaquenduri.prueba01_mqtt.Views.IAChatScreen
import com.kaquenduri.prueba01_mqtt.ui.theme.Prueba01MQTTTheme
import com.kaquenduri.prueba01_mqtt.utils.crearCanalNotificacion
import com.kaquenduri.prueba01_mqtt.Presentation.HomeScreen
import com.kaquenduri.prueba01_mqtt.Presentation.ScreenCultivo


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        crearCanalNotificacion(this)

        setContent {
            val configuracionViewModel: ConfiguracionVisualViewModel = viewModel()
            val colorPrimario by configuracionViewModel.colorPrimarioGuardado
            val colorSecundario by configuracionViewModel.colorSecundarioGuardado
            val tamañoTexto by configuracionViewModel.tamañoTextoGuardado

            Prueba01MQTTTheme(
                colorPrimario = colorPrimario,
                colorSecundario = colorSecundario,
                tamañoTexto = tamañoTexto
            ) {
                App()
            }
        }
    }
}

@Composable
fun App(navController: NavHostController = rememberNavController()) {
    val sensorViewModel: SensorViewModel = viewModel()
    val configuracionViewModel: ConfiguracionVisualViewModel = viewModel()
    val inicioViewModel : InicioViewModel = viewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    val showBars = currentDestination !in listOf("login", "register")

    Scaffold(
        topBar = { if (showBars) TopBar(navController, currentDestination) },
        bottomBar = { if (showBars) BottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                val loginViewModel: LoginViewModel = viewModel()
                val context = LocalContext.current
                LaunchedEffect(Unit) { loginViewModel.initRepository(context) }
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onGoToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                val registroViewModel: RegistroViewModel = viewModel()
                val context = LocalContext.current
                LaunchedEffect(Unit) { registroViewModel.initRepository(context) }
                RegistroScreen(
                    viewModel = registroViewModel,
                    RegistroExistoso = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    IrLogin = { navController.navigate("login") }
                )
            }

            composable("inicio"){

                InicioScreen(
                    viewModel = inicioViewModel
                )
            }

            composable("home") {
                val context = LocalContext.current
                LaunchedEffect(Unit) { sensorViewModel.initRepository(context) }
                HomeScreen(
                    viewModel = sensorViewModel,
                    navController = navController, // 👈 AÑADE ESTO
                    irAlertas = { navController.navigate("alertas") },
                    irConfiguracion = { navController.navigate("configuracion") }
                )

            }

            composable("alertas") {
                ConfiguracionAlertas(viewModel = sensorViewModel) {
                    navController.navigate("home")
                }
            }

            composable("configuracion") {
                ConfiguracionVisualScreen(viewModel = configuracionViewModel) {
                    navController.navigate("home")
                }
            }

            composable("ia_chat") { IAChatScreen() }
            composable("cultivo") {
                ScreenCultivo()
            }

        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController, currentRoute: String?) {
    val title = when (currentRoute) {
        "home" -> "Panel de Sensores"
        "configuracion" -> "Configuración Visual"
        "alertas" -> "Alertas"
        "ia_chat" -> "Asistente Agrícola"
        "inicio" -> "Bienvenido a AgroCode"
        else -> "Mi App de Sensores"
    }

    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF2E7D32),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = Modifier.padding(
            WindowInsets.statusBars.asPaddingValues() // 👈 agrega el espacio superior
        ),
        actions = {
            if (currentRoute == "home") {
                IconButton(onClick = { navController.navigate("alertas") }) {
                    Icon(Icons.Default.Warning, contentDescription = "Alertas")
                }
                IconButton(onClick = { navController.navigate("configuracion") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
                }
            }
        }
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentDestination == "home",
            onClick = {
                if (currentDestination != "home") {
                    navController.navigate("home") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Info, contentDescription = "IA Chat") },
            label = { Text("IA Chat") },
            selected = currentDestination == "ia_chat",
            onClick = {
                if (currentDestination != "ia_chat") {
                    navController.navigate("ia_chat") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
        // 🌱 Cultivo (tu nuevo botón)
        NavigationBarItem(
            icon = { Icon(Icons.Default.Info, contentDescription = "Cultivo") },
            label = { Text("Cultivo") },
            selected = false,
            onClick = {
                navController.navigate("cultivo") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            }
        )
    }
}
