package org.utl.reddeseguridadvecinal.modelo

data class CargoServicioDTO(
    val cargoServicioID: Int,
    val concepto: String,
    val monto: Double,
    val saldoPendiente: Double,
    val estado: String,
    val fechaCreacion: String
) {
    val montoPagado: Double
        get() = monto - saldoPendiente
}