package org.utl.reddeseguridadvecinal.modelo

data class DatosMesActual(
    val mantenimientoPagado: Double,
    val mantenimientoPendiente: Double,
    val serviciosPagado: Double,
    val serviciosPendiente: Double,
    val totalMantenimiento: Double,
    val totalServicios: Double
)