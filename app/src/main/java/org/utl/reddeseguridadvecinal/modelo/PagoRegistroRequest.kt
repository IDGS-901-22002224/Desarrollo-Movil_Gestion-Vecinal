package org.utl.reddeseguridadvecinal.modelo

data class PagoRegistroRequest(
    val usuarioID: Int,
    val montoTotal: Double,
    val tipoPago: String,
    val metodoPago: String,
    val detallesPagoJson: String
)