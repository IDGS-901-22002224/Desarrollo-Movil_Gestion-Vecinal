package org.utl.reddeseguridadvecinal.modelo

import java.util.Date

data class SolicitudServicio(
    val solicitudID: Int,
    val usuarioID: Int,
    val tipoServicioID: Int,
    val personaAsignado: Int?,
    val descripcion: String,
    val urgencia: String,
    val fechaPreferida: String?,
    val horaPreferida: String?,
    val estado: String,
    val fechaCreacion: String,
    val fechaAsignacion: String?,
    val fechaCompletado: String?,
    val notasAdmin: String?,
    val nombreUsuario: String?,
    val tipoServicioNombre: String?,
    val nombreAsignado: String?,
    val telefonoAsignado: String?
)