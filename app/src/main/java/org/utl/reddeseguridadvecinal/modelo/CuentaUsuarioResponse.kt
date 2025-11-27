package org.utl.reddeseguridadvecinal.modelo

data class CuentaUsuarioResponse(
    val cuentaID: Int,
    val saldoMantenimiento: Double,
    val saldoServicios: Double,
    val saldoTotal: Double,
    val ultimaActualizacion: String,
    val cargosMantenimiento: List<CargoMantenimientoDTO>,
    val cargosServicios: List<CargoServicioDTO>
)