package com.kaquenduri.prueba01_mqtt.models

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kaquenduri.prueba01_mqtt.ui.theme.Prueba01MQTTTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaquenduri.prueba01_mqtt.Presentation.HomeScreen
import com.kaquenduri.prueba01_mqtt.Presentation.LoginScreen
import com.kaquenduri.prueba01_mqtt.Presentation.RegistroScreen
import com.kaquenduri.prueba01_mqtt.ViewModels.HomeViewModel
import com.kaquenduri.prueba01_mqtt.ViewModels.LoginViewModel
import com.kaquenduri.prueba01_mqtt.ViewModels.RegistroViewModel


class MqttRepository (
    private val host: String,
    private val port: Int = 8883,
    private val clientId: String,
    private val username: String,
    private val password: String
) {
    private val client: Mqtt5AsyncClient = Mqtt5Client.builder()
        .identifier(clientId)
        .serverHost(host)
        .serverPort(port)
        .useSslWithDefaultConfig() // TLS
        .buildAsync()

    fun connect(onSuccess: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
        client.connectWith()
            .simpleAuth()
            .username(username)
            .password(password.toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    onFailure(throwable)
                } else {
                    onSuccess()
                }
            }
    }

    fun subscribe(topic: String, onMessage: (topic: String, message: String) -> Unit) {
        client.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { publish ->
                val msg = publish.payloadAsBytes?.decodeToString() ?: ""
                onMessage(publish.topic.toString(), msg)
            }
            .send()
    }

    fun publish(topic: String, message: String) {
        client.publishWith()
            .topic(topic)
            .payload(message.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
    }

    fun disconnect() {
        client.disconnect()
    }
}
