package com.kaquenduri.prueba01_mqtt.ViewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.kaquenduri.prueba01_mqtt.models.MqttRepository
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.Alerta
import com.kaquenduri.prueba01_mqtt.models.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*



class SensorViewModel(
    private val repository: MqttRepository = MqttRepository(
        host = "161.132.4.108",
        clientId = "android-client",
        username = "app_movil",
        password = "1234A5678a"
    )
) : ViewModel() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _mensajeHumedad = mutableStateOf("Esperando datos...")
    val mensajeHumedad: State<String> = _mensajeHumedad

    private val _mensajeTemperatura = mutableStateOf("Esperando datos...")
    val mensajeTemperatura: State<String> = _mensajeTemperatura

    private val _mensajeHumedadAire = mutableStateOf("Esperando datos...")
    val mensajeHumedadAire: State<String> = _mensajeHumedadAire

    private val _cultivo = MutableStateFlow("")
    val cultivo: StateFlow<String> = _cultivo

    // SENSORES SIMULADOS - Estado
    private val _phSuelo = mutableStateOf(0.0f)
    val phSuelo: State<Float> = _phSuelo

    private val _conductividad = mutableStateOf(0.0f)
    val conductividad: State<Float> = _conductividad

    private val _nitrogeno = mutableStateOf(0)
    val nitrogeno: State<Int> = _nitrogeno

    private val _fosforo = mutableStateOf(0)
    val fosforo: State<Int> = _fosforo

    private val _potasio = mutableStateOf(0)
    val potasio: State<Int> = _potasio

    private val _intensidadLuz = mutableStateOf(0)
    val intensidadLuz: State<Int> = _intensidadLuz

    // Estado de simulación
    private var simulacionJob: Job? = null
    private var cultivoActualId: Int? = null
    private var sensoresSimuladosActivos: Set<String> = emptySet()


    private val _alertaHumedadAlta = mutableStateOf(false)
    private val _alertaHumedadBaja = mutableStateOf(false)
    val alertaHumedadAlta: State<Boolean> = _alertaHumedadAlta
    val alertaHumedadBaja: State<Boolean> = _alertaHumedadBaja

    var alertaHumedadAltaActiva = mutableStateOf(true)
    var alertaHumedadBajaActiva = mutableStateOf(true)

    private val _estadoConexion = mutableStateOf("Desconectado")
    val estadoConexion: State<String> = _estadoConexion

    private val _conectado = mutableStateOf(false)
    val conectado: State<Boolean> = _conectado

    private lateinit var alertRepository: AlertRepository
    private val _alertas = mutableStateOf<List<Alerta>>(emptyList())
    val alertas: State<List<Alerta>> = _alertas

    private val _mensajesChat = mutableStateOf<List<MensajeChat>>(emptyList())
    val mensajesChat: State<List<MensajeChat>> = _mensajesChat

    private val _cargandoChat = mutableStateOf(false)
    val cargandoChat: State<Boolean> = _cargandoChat

    private val _ultimaHumedad = mutableStateOf(0)
    val ultimaHumedad: State<Int> = _ultimaHumedad

    private val ubicacionLima = GeoPoint(-12.0464, -77.0428)

    //  CORREGIDO: inicialización real del modelo Gemini
    private val geminiModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyAy8DDWIG0dQaJdn6UxudVoFpbtlqiqZkE" // Tu API real de Firebase JSON
        )
    }

    data class MensajeChat(
        val id: String = UUID.randomUUID().toString(),
        val texto: String,
        val esUsuario: Boolean,
        val timestamp: Long = System.currentTimeMillis(),
        val humedadActual: Int = 0
    )

    fun initRepository(context: Context) {
        val database = AppDatabase.getDatabase(context)
        alertRepository = AlertRepository(database.alertaDao())

        viewModelScope.launch {
            alertRepository.obtenerTodasAlertas().collect { listaAlertas ->
                _alertas.value = listaAlertas
            }
        }

        cargarMensajesExistentes()
        obtenerUltimaLectura()
    }

    fun enviarMensajeChat(mensaje: String) {
        if (mensaje.isBlank()) return

        viewModelScope.launch {
            obtenerUltimaLecturaYEsperar() // sensor_01

            // 🔹 Nuevo: obtenemos también el sensor_02
            val (humedadAire, temperatura) = obtenerLecturaSensor02()

            try {
                _cargandoChat.value = true
                val humedadActual = _ultimaHumedad.value

                val mensajeUsuario = MensajeChat(
                    texto = mensaje,
                    esUsuario = true,
                    humedadActual = humedadActual
                )

                _mensajesChat.value = _mensajesChat.value + mensajeUsuario

                // 🔹 Pasamos los nuevos datos al prompt contextual
                val promptContextual = crearPromptContextual(
                    preguntaUsuario = mensaje,
                    humedadAire = humedadAire,
                    temperatura = temperatura
                )

                val respuestaGemini = generarRespuestaConGemini(promptContextual)

                val mensajeIA = MensajeChat(
                    texto = respuestaGemini,
                    esUsuario = false,
                    humedadActual = humedadActual
                )

                _mensajesChat.value = _mensajesChat.value + mensajeIA
                guardarConversacionFirestore(mensaje, respuestaGemini)
            } catch (e: Exception) {
                Log.e("ChatGemini", "Error con Gemini: ${e.message}", e)
            } finally {
                _cargandoChat.value = false
            }
        }

    }

    private suspend fun obtenerUltimaLecturaYEsperar() {
        val task = db.collection("lecturas_datos").document("sensor_01").get()
        val document = task.await()
        if (document.exists()) {
            val humedad = document.getLong("humedad")?.toInt() ?: 0
            _ultimaHumedad.value = humedad
            Log.d("Sensor", " Lectura actualizada antes del chat: $humedad%")
        }
    }
    private suspend fun obtenerLecturaSensor02(): Pair<Int, Int> {
        val document = db.collection("lecturas_datos").document("sensor_02").get().await()
        return if (document.exists()) {
            val humedadAire = document.getLong("humedadAire")?.toInt() ?: 0
            val temperatura = document.getLong("temperatura")?.toInt() ?: 0
            Log.d("Sensor", " Sensor_02: humedadAire=$humedadAire, temperatura=$temperatura")
            Pair(humedadAire, temperatura)
        } else {
            Log.w("Sensor", "⚠ No existe el documento sensor_02")
            Pair(0, 0)
        }
    }


    private fun crearPromptContextual(
        preguntaUsuario: String,
        humedadAire: Int,
        temperatura: Int
    ): String {
        val humedadSuelo = _ultimaHumedad.value
        val ubicacion = "Arequipa, Perú"
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val cultivoActual = _cultivo.value.ifBlank { "cultivo general" }


        return """
        Eres un asistente experto en jardinería y agricultura que me brindaras datos 
        exactos y a al vez las recomendaciones sean mas especificas.  
        CONTEXTO:
        - Cultivo : $cultivoActual
        - Humedad del suelo: $humedadSuelo%
        - Humedad del aire: $humedadAire%
        - Temperatura ambiente: $temperatura°C
        - Ubicación: $ubicacion
        - Fecha/hora: $fecha

        PREGUNTA DEL USUARIO: "$preguntaUsuario"

        INSTRUCCIONES:
        1. Analiza las condiciones combinadas del suelo, aire y temperatura y toma en cuenta el cultivo indicado.
        2. Si la humedad del aire es muy baja (< 30%), sugiere riego ligero o sombra.
        3. Si la temperatura > 30°C, advierte sobre estrés térmico en plantas.
        4. Usa un tono natural, breve y con emojis relacionados con plantas 🌿☀💧.
        5. Basandote en la hora y dia recomendando horarios en los que hacer dichas acciones.
        6. Se preciso en las recomendaciones, da tiempos aproximados pero precisos 
        7. Que las respuestas esten bien organizadas y con un buen formato para que sea facil de leer.
        
    """.trimIndent()
    }


    private suspend fun generarRespuestaConGemini(prompt: String): String {
        return try {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash")

            val response = model.generateContent(prompt)
            response.text ?: " No pude generar una respuesta."
        } catch (e: Exception) {
            Log.e("GeminiAPI", "Error en generateContent", e)
            "⚠ Error al generar respuesta: ${e.message}"
        }
    }



    private fun guardarConversacionFirestore(pregunta: String, respuesta: String) {
        try {
            val fecha = Calendar.getInstance().time
            val fechaFormateada = SimpleDateFormat(
                "dd 'de' MMMM 'de' yyyy, h:mm:ss a",
                Locale.forLanguageTag("es-PE")
            ).format(fecha)

            val chatData = hashMapOf(
                "Fecha" to fechaFormateada,
                "Preguntas" to pregunta,
                "Respuestas" to respuesta,
                "Usuario" to "Jhunior",
                "timestamp" to System.currentTimeMillis(),
                "humedadActual" to _ultimaHumedad.value,
                "tipo" to "gemini_chat",
                "modelo" to "gemini-1.5-flash"
            )

            db.collection("Chat").add(chatData)
                .addOnSuccessListener {
                    Log.d("ChatGemini", " Conversación guardada en Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatGemini", " Error al guardar chat: ${e.message}")
                    // Puedes guardar localmente y sincronizar después
                }
        } catch (e: Exception) {
            Log.e("ChatGemini", " Error general: ${e.message}")
        }
    }

    private fun cargarMensajesExistentes() {
        db.collection("Chat")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { documents ->
                val mensajes = documents.flatMap { doc ->
                    val pregunta = doc.getString("Preguntas")
                    val respuesta = doc.getString("Respuestas")
                    val humedad = doc.getLong("humedadActual")?.toInt() ?: 0

                    listOfNotNull(
                        pregunta?.let {
                            MensajeChat(
                                id = doc.id + "_p",
                                texto = it,
                                esUsuario = true,
                                humedadActual = humedad
                            )
                        },
                        respuesta?.let {
                            MensajeChat(
                                id = doc.id + "_r",
                                texto = it,
                                esUsuario = false,
                                humedadActual = humedad
                            )
                        }
                    )
                }
                _mensajesChat.value = mensajes
                Log.d("ChatGemini", " ${mensajes.size} mensajes cargados")
            }
            .addOnFailureListener { e ->
                Log.e("ChatGemini", " Error al cargar historial", e)
            }
    }

    fun iniciarConexion() {
        _estadoConexion.value = "Conectando..."
        repository.connect(
            onSuccess = {
                _estadoConexion.value = "Conectado"
                _conectado.value = true
                repository.subscribe("sensor/humedad") { _, msg ->
                    try {
                        val json = JSONObject(msg)
                        val valorHumedad = json.getInt("humedad")
                        _mensajeHumedad.value = "Humedad: ${valorHumedad}%"
                        _ultimaHumedad.value = valorHumedad
                        guardarLecturaHumedad(valorHumedad)

                        val alta = alertaHumedadAltaActiva.value && valorHumedad > 80
                        val baja = alertaHumedadBajaActiva.value && valorHumedad < 20
                        _alertaHumedadAlta.value = alta
                        _alertaHumedadBaja.value = baja

                        if (::alertRepository.isInitialized && (alta || baja)) {
                            viewModelScope.launch {
                                val textoAlerta = if (alta) "¡Humedad muy alta! (${valorHumedad}%). Suspende tu riego"
                                else "¡Suelo muy seco! (${valorHumedad}%). Riega con un poco de agua"
                                alertRepository.guardarAlerta(textoAlerta)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MQTT", "Error procesando mensaje: $msg", e)
                    }

                repository.subscribe("sensor/temperatura") { _, msg ->
                    try {
                        val json = JSONObject(msg)
                        val valorHumedad = json.getInt("temperatura")
                        _mensajeTemperatura.value = "Temperatura: ${valorHumedad}%"


                    } catch (e: Exception) {
                        Log.e("MQTT", "Error procesando mensaje: $msg", e)
                    }
                }

                repository.subscribe("sensor/humedadAire") { _, msg ->
                    try {
                        val json = JSONObject(msg)
                        val valorHumedad = json.getInt("humedadAire")
                        _mensajeHumedadAire.value = "HumedadAire: ${valorHumedad}%"


                    } catch (e: Exception) {
                        Log.e("MQTT", "Error procesando mensaje: $msg", e)
                    }
                }
                }
            },
            onFailure = {
                _estadoConexion.value = "Error de conexión"
                _conectado.value = false
                _mensajeTemperatura.value = "Error: ${it.message}"
            }
        )
    }

    fun guardarLecturaHumedad(valor: Int) {
        val fecha = Calendar.getInstance().time
        val fechaFormateada = SimpleDateFormat(
            "dd 'de' MMMM 'de' yyyy, h:mm:ss a",
            Locale.forLanguageTag("es-PE")
        ).format(fecha)

        // Datos de la lectura
        val lecturaData = hashMapOf(
            "fecha" to fechaFormateada,
            "humedad" to valor,
            "ubicacion" to ubicacionLima,
            "timestamp" to System.currentTimeMillis()
        )

        //  NUEVO: guardar como registro histórico (no sobrescribir)
        db.collection("cultivos")
            .document("cultivo_id_123") // ← cambia por tu ID real o dinámico
            .collection("lecturas")     // subcolección de lecturas históricas
            .add(lecturaData)
            .addOnSuccessListener { doc ->
                Log.d("Firestore", " Lectura guardada: ${doc.id} → $valor%")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", " Error al guardar lectura", e)
            }

        //  (Opcional) también actualizar el último valor si quieres mantenerlo visible
        db.collection("lecturas_datos")
            .document("sensor_01")
            .set(lecturaData)
            .addOnSuccessListener { Log.d("Sensor", "ℹ Última lectura actualizada $valor%") }
    }

    fun guardarLecturaCompleta(
        sensorId: String = "sensor_01",  // 🔹 puedes cambiar dinámicamente según el sensor
        humedad: Int,
        temperatura: Int,
        humedadAire: Int
    ) {
        val db = FirebaseFirestore.getInstance()

        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val timestamp = System.currentTimeMillis()

        val lecturaData = hashMapOf(
            "fecha" to fechaActual,
            "humedad" to humedad,
            "temperatura" to temperatura,
            "humedadAire" to humedadAire,
            "timestamp" to timestamp,
            "ubicacion" to listOf("-12.0464° S", "-77.0428° W") // opcional, igual que ya usas
        )

        // 🔹 Guardamos dentro del documento sensor_01 o sensor_02
        db.collection("lecturas_datos")
            .document(sensorId)
            .collection("lecturas")  // subcolección donde se guardan todas las lecturas
            .add(lecturaData)
            .addOnSuccessListener {
                println(" Lectura guardada correctamente en $sensorId")
            }
            .addOnFailureListener { e ->
                println(" Error al guardar lectura en $sensorId: $e")
            }
    }

    fun desconectar() {
        repository.disconnect()
        _estadoConexion.value = "Desconectado"
        _conectado.value = false
        _mensajeTemperatura.value = "Esperando datos..."
    }

    fun limpiarChat() {
        _mensajesChat.value = emptyList()
    }

    fun probarGemini() {
        enviarMensajeChat("Hola, ¿cómo estás? Dame un análisis de la humedad actual.")
    }

    fun obtenerUltimaLectura() {
        db.collection("lecturas_datos").document("sensor_01").get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val humedad = document.getLong("humedad")?.toInt() ?: 0
                    val fecha = document.getString("fecha") ?: "N/A"
                    _ultimaHumedad.value = humedad
                    _mensajeTemperatura.value = "Última lectura: ${humedad}% (${fecha})"
                    Log.d("Sensor", " Última lectura recuperada: $humedad%")
                } else {
                    Log.w("Sensor", " No existe el documento sensor_01")
                }
            }
            .addOnFailureListener { e -> Log.e("Sensor", " Error al obtener última lectura")}
    }

    fun onCultivoChange(nuevoValor: String) {
        _cultivo.value = nuevoValor
    }

    // ========== SISTEMA DE SIMULACIÓN DE SENSORES ==========
    
    /**
     * Inicia la simulación de sensores para un cultivo específico
     * @param cultivoId ID del cultivo
     * @param sensoresActivos Set de sensores simulados activos (ej: ["ph", "conductividad", "nutrientes", "luz"])
     */
    fun iniciarSimulacionSensores(cultivoId: Int, sensoresActivos: Set<String>) {
        detenerSimulacionSensores()
        cultivoActualId = cultivoId
        sensoresSimuladosActivos = sensoresActivos

        if (sensoresActivos.isEmpty()) return

        // Inicializar valores base realistas
        _phSuelo.value = 6.5f + (Math.random().toFloat() * 1.0f - 0.5f) // 6.0-7.0
        _conductividad.value = 1.2f + (Math.random().toFloat() * 0.6f - 0.3f) // 0.9-1.5 mS/cm
        _nitrogeno.value = 50 + (Math.random() * 30).toInt() // 50-80 ppm
        _fosforo.value = 20 + (Math.random() * 15).toInt() // 20-35 ppm
        _potasio.value = 150 + (Math.random() * 50).toInt() // 150-200 ppm
        _intensidadLuz.value = calcularIntensidadLuzCircadiana()

        simulacionJob = viewModelScope.launch {
            while (isActive) {
                delay(1000) // Actualizar cada 5 segundos
                
                // Generar variaciones realistas
                if ("ph" in sensoresActivos) {
                    _phSuelo.value = generarValorPhRealista(_phSuelo.value)
                }
                if ("conductividad" in sensoresActivos) {
                    _conductividad.value = generarValorConductividadRealista(_conductividad.value)
                }
                if ("nutrientes" in sensoresActivos) {
                    _nitrogeno.value = generarValorNutrienteRealista(_nitrogeno.value, 50, 80)
                    _fosforo.value = generarValorNutrienteRealista(_fosforo.value, 20, 35)
                    _potasio.value = generarValorNutrienteRealista(_potasio.value, 150, 200)
                }
                if ("luz" in sensoresActivos) {
                    _intensidadLuz.value = calcularIntensidadLuzCircadiana()
                }
            }
        }
    }

    /**
     * Detiene la simulación de sensores
     */
    fun detenerSimulacionSensores() {
        simulacionJob?.cancel()
        simulacionJob = null
        cultivoActualId = null
        sensoresSimuladosActivos = emptySet()
    }

    /**
     * Genera un valor de pH realista con variación suave (6.0-7.5)
     */
    private fun generarValorPhRealista(valorActual: Float): Float {
        val variacion = (Math.random().toFloat() * 0.2f - 0.1f) // ±0.1
        val nuevoValor = valorActual + variacion
        return nuevoValor.coerceIn(6.0f, 7.5f)
    }

    /**
     * Genera un valor de conductividad eléctrica realista (0.8-2.0 mS/cm)
     */
    private fun generarValorConductividadRealista(valorActual: Float): Float {
        val variacion = (Math.random().toFloat() * 0.15f - 0.075f) // ±0.075
        val nuevoValor = valorActual + variacion
        return nuevoValor.coerceIn(0.8f, 2.0f)
    }

    /**
     * Genera un valor de nutriente realista con variación natural
     */
    private fun generarValorNutrienteRealista(valorActual: Int, min: Int, max: Int): Int {
        val variacion = (Math.random() * 6 - 3).toInt() // ±3
        val nuevoValor = valorActual + variacion
        return nuevoValor.coerceIn(min, max)
    }

    /**
     * Calcula la intensidad lumínica basada en la hora del día (simulación circadiana)
     * Retorna valores de 0-100 (0 = noche, 100 = mediodía)
     */
    private fun calcularIntensidadLuzCircadiana(): Int {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val minuto = calendar.get(Calendar.MINUTE)
        val horaDecimal = hora + (minuto / 60.0f)

        // Simulación de ciclo diurno:
        // 6:00 AM - inicio del día (0-20%)
        // 8:00 AM - mañana (20-60%)
        // 12:00 PM - mediodía (60-100%)
        // 6:00 PM - tarde (100-60%)
        // 8:00 PM - atardecer (60-20%)
        // 10:00 PM - noche (20-0%)

        return when {
            horaDecimal < 6 -> 0
            horaDecimal < 8 -> ((horaDecimal - 6) / 2 * 20).toInt() // 0-20%
            horaDecimal < 12 -> (20 + ((horaDecimal - 8) / 4 * 40)).toInt() // 20-60%
            horaDecimal < 14 -> (60 + ((horaDecimal - 12) / 2 * 40)).toInt() // 60-100%
            horaDecimal < 18 -> (100 - ((horaDecimal - 14) / 4 * 40)).toInt() // 100-60%
            horaDecimal < 20 -> (60 - ((horaDecimal - 18) / 2 * 40)).toInt() // 60-20%
            horaDecimal < 22 -> (20 - ((horaDecimal - 20) / 2 * 20)).toInt() // 20-0%
            else -> 0
        }.coerceIn(0, 100)
    }

    /**
     * Obtiene el mensaje formateado para el sensor de pH
     */
    fun obtenerMensajePh(): String {
        return "pH: ${String.format("%.1f", _phSuelo.value)}"
    }

    /**
     * Obtiene el mensaje formateado para el sensor de conductividad
     */
    fun obtenerMensajeConductividad(): String {
        return "Conductividad: ${String.format("%.2f", _conductividad.value)} mS/cm"
    }

    /**
     * Obtiene el mensaje formateado para los nutrientes NPK
     */
    fun obtenerMensajeNutrientes(): String {
        return "N:${_nitrogeno.value} P:${_fosforo.value} K:${_potasio.value} ppm"
    }

    /**
     * Obtiene el mensaje formateado para la intensidad lumínica
     */
    fun obtenerMensajeLuz(): String {
        return "Luz: ${_intensidadLuz.value}%"
    }
}