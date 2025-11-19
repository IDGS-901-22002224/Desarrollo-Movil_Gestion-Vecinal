package org.utl.reddeseguridadvecinal.modelo

data class ReporteRequest(
    val usuarioID: Int?,
    val tipoReporteID: Int,
    val titulo: String?,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val direccionTexto: String,
    val esAnonimo: Boolean,
    val imagenBase64: String?
)