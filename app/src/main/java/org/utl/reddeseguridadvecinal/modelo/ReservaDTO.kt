// ReservaDTO.kt
package org.utl.reddeseguridadvecinal.modelo

data class ReservaDTO(
    val reservaID: Int,
    val usuarioID: Int,
    val amenidadID: Int,
    val fechaReserva: String,
    val horaInicio: String,
    val horaFin: String,
    val motivo: String,
    val fechaCreacion: String,
    val amenidadNombre: String,
    val tipoAmenidad: String,
    val nombreUsuario: String,
    val estado: String
)