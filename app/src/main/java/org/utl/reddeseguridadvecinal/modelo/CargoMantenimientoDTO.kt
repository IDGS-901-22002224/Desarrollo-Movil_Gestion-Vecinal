package org.utl.reddeseguridadvecinal.modelo

data class CargoMantenimientoDTO(
    val cargoMantenimientoID: Int,
    val concepto: String,
    val monto: Double,
    val saldoPendiente: Double,
    val estado: String,
    var seleccionadoParaPago: Boolean = false,
    val fechaVencimiento: String
) {
    val montoPagado: Double
        get() = monto - saldoPendiente
}