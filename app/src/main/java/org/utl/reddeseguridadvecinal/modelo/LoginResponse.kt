package org.utl.reddeseguridadvecinal.modelo

data class LoginResponse(
    val message: String,
    val id: Int,
    val nombre: String,
    val apellidoP: String,
    val apellidoM: String,
    val tipoUsuario: String,
    val numeroCasa: String,
    val calle: String,
    val firebaseID: String
)