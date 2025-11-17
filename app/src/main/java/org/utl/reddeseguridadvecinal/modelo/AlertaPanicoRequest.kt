package org.utl.reddeseguridadvecinal.modelo

data class AlertaPanicoRequest(
    val usuarioID: Int,
    val latitud: Double,
    val longitud: Double
)