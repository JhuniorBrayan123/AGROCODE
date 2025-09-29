package com.example.agrocode.domain.model

data class Usuario(
    val id: String = "",
    val correo: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val fechaCreacion: String = ""
)

data class CredencialesLogin(
    val correo: String = "",
    val contrasena: String = ""
)

data class DatosRegistro(
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = ""
)
