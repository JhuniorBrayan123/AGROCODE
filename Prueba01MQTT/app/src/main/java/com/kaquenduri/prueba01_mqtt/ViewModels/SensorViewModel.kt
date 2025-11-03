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
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*



class SensorViewModel(
    private val repository: MqttRepository = MqttRepository(
        host = "73bf3958395f4c728c926313efe629f1.s1.eu.hivemq.cloud",
        clientId = "android-client",
        username = "client-app",
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

    // ✅ CORREGIDO: inicialización real del modelo Gemini
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
                Log.e("ChatGemini", "❌ Error con Gemini: ${e.message}", e)
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
            .addOnSuccessListener { Log.d("ChatGemini", " Conversación guardada en Firestore") }
            .addOnFailureListener { e -> Log.e("ChatGemini", " Error al guardar chat", e) }
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
        val fechaFormateada = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, h:mm:ss a", Locale.forLanguageTag("es-PE")).format(fecha)
        val lecturaData = hashMapOf(
            "fecha" to fechaFormateada,
            "humedad" to valor,
            "ubicacion" to ubicacionLima,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("lecturas_datos").document("sensor_01").set(lecturaData)
            .addOnSuccessListener { Log.d("Sensor", "✅ Lectura guardada en sensor_01: $valor%") }
            .addOnFailureListener { e -> Log.e("Sensor", "❌ Error al guardar lectura", e) }
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
                    Log.d("Sensor", "✅ Última lectura recuperada: $humedad%")
                } else {
                    Log.w("Sensor", "⚠ No existe el documento sensor_01")
                }
            }
            .addOnFailureListener { e -> Log.e("Sensor", "❌ Error al obtener última lectura")}
    }

    fun onCultivoChange(nuevoValor: String) {
        _cultivo.value = nuevoValor
    }
}