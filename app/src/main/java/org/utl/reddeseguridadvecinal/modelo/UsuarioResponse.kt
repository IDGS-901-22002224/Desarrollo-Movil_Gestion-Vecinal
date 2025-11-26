package org.utl.reddeseguridadvecinal.modelo

data class UsuarioResponse(
    val usuarioID: Int,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val telefono: String?,
    val email: String?,
    val fechaNacimiento: String?,
    val tipoUsuario: String,
    val numeroCasa: String,
    val calle: String,
    val activo: Boolean,

    val numeroTarjeta: String?,
    val ultimosDigitos: String?,
    val fechaVencimiento: String?
)