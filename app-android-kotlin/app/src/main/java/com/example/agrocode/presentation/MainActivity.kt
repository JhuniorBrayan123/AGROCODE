package com.example.agrocode.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agrocode.presentation.features.login.LoginScreen
import com.example.agrocode.presentation.features.login.LoginScreenPreview
import com.example.agrocode.presentation.features.login.LoginViewModel

import com.example.agrocode.presentation.theme.AGROCODETheme
import com.example.agrocode.presentation.theme.WhiteBackground
import com.example.agrocode.presentation.theme.GreenPrimary



class MainActivity : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AGROCODETheme {
                LoginScreen(LoginViewModel())
            }
        }
    }
}


