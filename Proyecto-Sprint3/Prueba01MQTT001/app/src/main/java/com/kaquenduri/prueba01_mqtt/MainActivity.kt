// 🔹 Archivo: MainActivity.kt
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable as animComposable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.kaquenduri.prueba01_mqtt.Presentation.*
import com.kaquenduri.prueba01_mqtt.ViewModels.*
import com.kaquenduri.prueba01_mqtt.Views.IAChatScreen
import com.kaquenduri.prueba01_mqtt.ui.theme.Prueba01MQTTTheme
import com.kaquenduri.prueba01_mqtt.utils.crearCanalNotificacion
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "app_open")
        }
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)

        enableEdgeToEdge()
        crearCanalNotificacion(this)
        //  Crash de prueba para verificar que Crashlytics está activo
        // val crashlytics = FirebaseCrashlytics.getInstance()
        //crashlytics.log("Probando Crashlytics desde MainActivity")
        //throw RuntimeException("Crash de prueba para Firebase Crashlytics")
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App(navController: NavHostController = rememberNavController()) {
    val sensorViewModel: SensorViewModel = viewModel()
    val configuracionViewModel: ConfiguracionVisualViewModel = viewModel()
    val inicioViewModel: InicioViewModel = viewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    val showBars = currentDestination !in listOf("login", "register", "inicio")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (showBars) {
                ModalDrawerSheet {
                    Text(
                        text = "Menú",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Mis Cultivos") },
                        selected = currentDestination == "listaCultivos" || currentDestination?.startsWith("dashboardCultivo") == true,
                        onClick = {
                            navController.navigate("listaCultivos") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Crear Cultivo") },
                        selected = currentDestination == "crearCultivo",
                        onClick = {
                            navController.navigate("crearCultivo") { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("IA Chat") },
                        selected = currentDestination == "ia_chat",
                        onClick = {
                            navController.navigate("ia_chat") { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Configuración Visual") },
                        selected = currentDestination == "configuracion",
                        onClick = {
                            navController.navigate("configuracion") { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Alertas") },
                        selected = currentDestination == "alertas",
                        onClick = {
                            navController.navigate("alertas") { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Todas las Actividades") },
                        selected = currentDestination == "listaActividadesGlobal",
                        onClick = {
                            navController.navigate("listaActividadesGlobal") { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        }
                    )

                }
            }
        }
    ) {
        Scaffold(
            topBar = { 
                if (showBars && currentDestination != "ia_chat") {
                    TopBar(navController, currentDestination) { scope.launch { drawerState.open() } } 
                }
            },
            bottomBar = { if (showBars) BottomBar(navController) }
        ) { innerPadding ->
        AnimatedNavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) {
            animComposable("login") {
                val loginViewModel: LoginViewModel = viewModel()
                val context = LocalContext.current
                LaunchedEffect(Unit) { loginViewModel.initRepository(context) }
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onGoToRegister = { navController.navigate("register") }
                )
            }

            animComposable("register") {
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

            animComposable("inicio") {
                InicioScreen(viewModel = inicioViewModel, navController = navController)
            }

            // ========== NUEVAS RUTAS PRINCIPALES ==========
            
            // Lista de cultivos (pantalla principal después del login)
            animComposable("listaCultivos") {
                ListaCultivosScreen(
                    navController = navController,
                    userId = 1 // TODO: Obtener desde login o SharedPreferences
                )
            }

            // Dashboard específico de cultivo
            animComposable("dashboardCultivo/{cultivoId}") { backStackEntry ->
                val cultivoId = backStackEntry.arguments?.getString("cultivoId")?.toIntOrNull() ?: 0
                val context = LocalContext.current
                LaunchedEffect(Unit) { sensorViewModel.initRepository(context) }
                DashboardCultivoScreen(
                    cultivoId = cultivoId,
                    navController = navController,
                    sensorViewModel = sensorViewModel
                )
            }

            // Crear nuevo cultivo
            animComposable("crearCultivo") {
                ScreenCultivo(
                    navController = navController,
                    userId = 1 // TODO: Obtener desde login o SharedPreferences
                )
            }

            // Editar cultivo existente
            animComposable("editarCultivo/{cultivoId}") { backStackEntry ->
                val cultivoId = backStackEntry.arguments?.getString("cultivoId")?.toIntOrNull() ?: 0
                EditarCultivoScreen(
                    navController = navController,
                    cultivoId = cultivoId
                )
            }

            // ========== RUTAS CONTEXTUALES POR CULTIVO ==========
            
            animComposable("estadisticas/{cultivoId}") { backStackEntry ->
                val cultivoId = backStackEntry.arguments?.getString("cultivoId")?.toIntOrNull() ?: 0
                EstadisticasScreen(viewModel = sensorViewModel) {
                    navController.navigate("dashboardCultivo/$cultivoId")
                }
            }

            animComposable("graficos/{cultivoId}") { backStackEntry ->
                val cultivoId = backStackEntry.arguments?.getString("cultivoId")?.toIntOrNull() ?: 0
                GraficosScreen(
                    viewModel = sensorViewModel,
                    irHome = {
                        navController.navigate("dashboardCultivo/$cultivoId")
                    },
                    cultivoId = cultivoId
                )
            }

            animComposable("registroActividad/{cultivoId}") { backStackEntry ->
                val cultivoId = backStackEntry.arguments?.getString("cultivoId")?.toIntOrNull() ?: 0
                RegistroActividadScreen(
                    navController = navController,
                    cultivoId = cultivoId
                )
            }

            // Lista global de actividades (todas las actividades de todos los cultivos)
            animComposable("listaActividadesGlobal") {
                ListaActividadesGlobalScreen(
                    navController = navController,
                    userId = 1 // TODO: Obtener desde login o SharedPreferences
                )
            }


            // ========== RUTAS GLOBALES (MANTENER) ==========

            // 👈 MANTENER home original como fallback (compatibilidad)
            animComposable("home") {
                val context = LocalContext.current
                LaunchedEffect(Unit) { sensorViewModel.initRepository(context) }
                HomeScreen(
                    viewModel = sensorViewModel,
                    navController = navController,
                    cultivoId = 0, // 👈 CORREGIDO: valor por defecto
                    irAlertas = { navController.navigate("alertas") },
                    irConfiguracion = { navController.navigate("configuracion") }
                )
            }

            // En MainActivity.kt - CORREGIR LAS RUTAS home
            animComposable("home/{cultivoId}") { backStackEntry ->
                val cultivoId = backStackEntry.arguments?.getString("cultivoId")?.toIntOrNull() ?: 0
                val context = LocalContext.current
                LaunchedEffect(Unit) { sensorViewModel.initRepository(context) }
                HomeScreen(
                    viewModel = sensorViewModel,
                    navController = navController,
                    cultivoId = cultivoId, // 👈 CORREGIDO: usar el parámetro
                    irAlertas = { navController.navigate("alertas") },
                    irConfiguracion = { navController.navigate("configuracion") }
                )
            }

            animComposable("alertas") {
                ConfiguracionAlertas(viewModel = sensorViewModel) {
                    navController.navigate("listaCultivos")
                }
            }

            animComposable("configuracion") {
                ConfiguracionVisualScreen(viewModel = configuracionViewModel) {
                    navController.navigate("listaCultivos")
                }
            }

            // IA Chat con contexto de cultivo
            animComposable("ia_chat") {
                IAChatScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            // IA Chat - la ruta con cultivoId se maneja en el código de navegación

            // En MainActivity.kt - MANTENER cultivo para compatibilidad
            animComposable("cultivo") {
                ScreenCultivo(
                    navController = navController,
                    userId = 1
                )
            }

            // Rutas sin cultivoId (compatibilidad - redirigen a lista)
            animComposable("graficos") {
                GraficosScreen(
                    viewModel = sensorViewModel,
                    irHome = {
                        navController.navigate("listaCultivos")
                    },
                    cultivoId = 0
                )
            }

            animComposable("estadisticas") {
                EstadisticasScreen(viewModel = sensorViewModel) {
                    navController.navigate("listaCultivos")
                }
            }

            animComposable("limites") {
                ConfiguracionLimitesScreen(viewModel = sensorViewModel) {
                    navController.navigate("listaCultivos")
                }
            }

            animComposable("alertas_sonoras") {
                ConfiguracionSonoraScreen {
                    navController.navigate("listaCultivos")
                }
            }

            animComposable("ordenar_modulos") {
                OrdenarModulosScreen {
                    navController.navigate("listaCultivos")
                }
            }

            animComposable("notificaciones_push") {
                NotificacionesPushScreen {
                    navController.navigate("listaCultivos")
                }
            }

            animComposable("recomendaciones_acciones") {
                RecomendacionesAccionesScreen(viewModel = sensorViewModel) {
                    navController.navigate("listaCultivos")
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController, currentRoute: String?, onMenuClick: () -> Unit) {
    val title = when {
        currentRoute?.startsWith("dashboardCultivo") == true -> "Dashboard de Cultivo"
        currentRoute?.startsWith("estadisticas") == true -> "Estadísticas"
        currentRoute?.startsWith("graficos") == true -> "Gráficos"
        currentRoute?.startsWith("registroActividad") == true -> "Registro de Actividades"
        currentRoute == "listaCultivos" -> "Mis Cultivos"
        currentRoute == "crearCultivo" -> "Crear Cultivo"
        currentRoute == "editarCultivo" -> "Editar Cultivo"
        currentRoute == "home" -> "Panel de Sensores"
        currentRoute == "configuracion" -> "Configuración Visual"
        currentRoute == "alertas" -> "Alertas"
        currentRoute == "limites" -> "Límites Personalizados"
        currentRoute == "alertas_sonoras" -> "Alertas Sonoras"
        currentRoute == "ordenar_modulos" -> "Ordenar Módulos"
        currentRoute == "notificaciones_push" -> "Notificaciones Push"
        currentRoute == "recomendaciones_acciones" -> "Recomendaciones de Acciones"
        currentRoute == "ia_chat" -> "Asistente Agrícola"
        currentRoute == "inicio" -> "Bienvenido a AgroCode"
        currentRoute == "cultivo" -> "Gestión de Cultivos"
        currentRoute == "listaActividadesGlobal" -> "Todas las Actividades"
        else -> "AgroCode"
    }

    // Determinar si mostrar menú hamburguesa o flecha de regreso
    // Rutas principales: home, listaCultivos, inicio -> mostrar menú
    // Todas las demás rutas -> mostrar flecha de regreso
    val rutasPrincipales = listOf("home", "listaCultivos", "inicio")
    val mostrarMenu = currentRoute in rutasPrincipales
    val puedeRegresar = navController.previousBackStackEntry != null

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (mostrarMenu) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menú")
                }
            } else if (puedeRegresar) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF2E7D32),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = Modifier.padding(
            WindowInsets.statusBars.asPaddingValues()
        ),
        actions = {
            if (currentRoute == "home") {
                IconButton(onClick = { navController.navigate("alertas") }) {
                    Icon(Icons.Filled.Warning, contentDescription = "Alertas")
                }
                IconButton(onClick = { navController.navigate("configuracion") }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Configuración", tint = Color.White)
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
            icon = { Icon(Icons.Filled.Home, contentDescription = "Cultivos") },
            label = { Text("Cultivos") },
            selected = currentDestination == "listaCultivos" || currentDestination?.startsWith("dashboardCultivo") == true,
            onClick = {
                if (currentDestination != "listaCultivos") {
                    navController.navigate("listaCultivos") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Info, contentDescription = "IA Chat") },
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
        // 🌱 Cultivo
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Info, contentDescription = "Cultivo") },
            label = { Text("Cultivo") },
            selected = currentDestination == "cultivo",
            onClick = {
                navController.navigate("cultivo") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            }
        )
    }
}
