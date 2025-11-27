package org.utl.reddeseguridadvecinal.modelo

data class DatosGraficaPagos(
    val totalMantenimiento: Double,
    val totalServicios: Double,
    val porcentajeMantenimiento: Float,
    val porcentajeServicios: Float,
    val anguloMantenimiento: Float,
    val anguloServicios: Float
)