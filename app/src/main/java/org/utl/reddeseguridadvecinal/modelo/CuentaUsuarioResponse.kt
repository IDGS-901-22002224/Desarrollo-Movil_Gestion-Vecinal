<<<<<<< HEAD
package org.utl.reddeseguridadvecinal.modelo

data class CuentaUsuarioResponse(
    val cuentaID: Int,
    val saldoMantenimiento: Double,
    val saldoServicios: Double,
    val saldoTotal: Double,
    val ultimaActualizacion: String,
    val cargosMantenimiento: List<CargoMantenimientoDTO>,
    val cargosServicios: List<CargoServicioDTO>
=======
package org.utl.reddeseguridadvecinal.modelo

data class CuentaUsuarioResponse(
    val cuentaID: Int,
    val saldoMantenimiento: Double,
    val saldoServicios: Double,
    val saldoTotal: Double,
    val ultimaActualizacion: String,
    val cargosMantenimiento: List<CargoMantenimientoDTO>,
    val cargosServicios: List<CargoServicioDTO>
>>>>>>> 78d914d85a3eb3b4bfa69f32dfd5a85ccf02eb3a
)