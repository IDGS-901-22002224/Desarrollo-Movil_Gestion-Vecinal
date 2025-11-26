package org.utl.reddeseguridadvecinal.modelo

data class UsuarioUpdateRequest(
    val usuarioID: Int,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val numeroCasa: String,
    val calle: String,
    val telefono: String,
    val fechaNacimiento: String,
    val email: String,
    val password: String,
    val numeroTarjeta: String,
    val ultimosDigitos: String,
    val fechaVencimiento: String
)