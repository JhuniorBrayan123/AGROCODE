package com.example.agrocode.data.repository

import com.example.agrocode.domain.model.CredencialesLogin
import com.example.agrocode.domain.model.DatosRegistro
import com.example.agrocode.domain.model.Usuario
import com.example.agrocode.domain.usecase.ResultadoLogin
import kotlinx.coroutines.delay

class UsuarioRepositoryImpl : UsuarioRepository {
    
    override suspend fun iniciarSesion(credenciales: CredencialesLogin): ResultadoLogin {
        // Simular llamada a API
        delay(1000)
        
        return when {
            credenciales.correo.isEmpty() -> ResultadoLogin.Error("El correo es requerido")
            credenciales.contrasena.isEmpty() -> ResultadoLogin.Error("La contraseña es requerida")
            !esCorreoValido(credenciales.correo) -> ResultadoLogin.Error("Formato de correo inválido")
            credenciales.contrasena.length < 6 -> ResultadoLogin.Error("La contraseña debe tener al menos 6 caracteres")
            else -> {
                // Simular login exitoso
                val usuario = Usuario(
                    id = "1",
                    correo = credenciales.correo,
                    nombre = "Usuario",
                    apellido = "Demo",
                    telefono = "+1234567890",
                    fechaCreacion = "2024-01-01"
                )
                ResultadoLogin.Exitoso(usuario)
            }
        }
    }
    
    override suspend fun registrarUsuario(datosRegistro: DatosRegistro): ResultadoLogin {
        // Simular llamada a API
        delay(1000)
        
        return when {
            datosRegistro.correo.isEmpty() -> ResultadoLogin.Error("El correo es requerido")
            datosRegistro.contrasena.isEmpty() -> ResultadoLogin.Error("La contraseña es requerida")
            datosRegistro.nombre.isEmpty() -> ResultadoLogin.Error("El nombre es requerido")
            datosRegistro.apellido.isEmpty() -> ResultadoLogin.Error("El apellido es requerido")
            !esCorreoValido(datosRegistro.correo) -> ResultadoLogin.Error("Formato de correo inválido")
            datosRegistro.contrasena.length < 6 -> ResultadoLogin.Error("La contraseña debe tener al menos 6 caracteres")
            datosRegistro.contrasena != datosRegistro.confirmarContrasena -> ResultadoLogin.Error("Las contraseñas no coinciden")
            else -> {
                // Simular registro exitoso
                val usuario = Usuario(
                    id = "2",
                    correo = datosRegistro.correo,
                    nombre = datosRegistro.nombre,
                    apellido = datosRegistro.apellido,
                    telefono = datosRegistro.telefono,
                    fechaCreacion = "2024-01-01"
                )
                ResultadoLogin.Exitoso(usuario)
            }
        }
    }
    
    private fun esCorreoValido(correo: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }
}
