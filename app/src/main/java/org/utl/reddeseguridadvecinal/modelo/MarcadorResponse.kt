package org.utl.reddeseguridadvecinal.modelo

data class MarcadorResponse(
    val latitud: Double,
    val longitud: Double,
    val indicador: String,
    val comentario: String
)