package org.utl.reddeseguridadvecinal.modelo

data class SolicitudServicioRequest(
    val request: SolicitudServicioData? = null,
    val usuarioID: Int,
    val tipoServicioID: Int,
    val descripcion: String,
    val urgencia: String,
    val fechaPreferida: String,
    val horaPreferida: String
)