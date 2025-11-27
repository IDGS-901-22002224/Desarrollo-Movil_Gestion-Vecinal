package org.utl.reddeseguridadvecinal.modelo

data class DetallePagoRequest(
    val montoAplicado: Double,
    val cargoMantenimientoID: Int? = null,
    val cargoServicioID: Int? = null
)