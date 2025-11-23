package org.utl.reddeseguridadvecinal.modelo

data class SolicitudServicioData(
    val usuarioID: Int,
    val tipoServicioID: Int,
    val descripcion: String,
    val urgencia: String,
    val fechaPreferida: String,
    val horaPreferida: String
)