
package org.utl.reddeseguridadvecinal.modelo

data class CreateReservaRequest(
    val usuarioID: Int,
    val amenidadID: Int,
    val fechaReserva: String,
    val horaInicio: String,
    val horaFin: String,
    val motivo: String
)