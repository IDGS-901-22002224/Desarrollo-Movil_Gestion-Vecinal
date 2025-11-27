package org.utl.reddeseguridadvecinal.modelo

data class CargoServicio(
    val cargoServicioID: Int,
    val usuarioID: Int,
    val solicitudID: Int,
    val concepto: String,
    val monto: Double,
    val estado: String,
    val montoPagado: Double,
    val saldoPendiente: Double,
    val fechaCreacion: String,
    val descripcionSolicitud: String? = null,
    val nombreUsuario: String? = null
)