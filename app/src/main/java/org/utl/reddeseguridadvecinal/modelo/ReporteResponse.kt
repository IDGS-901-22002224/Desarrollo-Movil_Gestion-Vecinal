package org.utl.reddeseguridadvecinal.modelo

import com.google.gson.annotations.SerializedName

data class ReporteResponse(
    val reporteID: Int,
    val descripcion: String,
    val direccionTexto: String,
    val fechaCreacion: String,
    val visto: Boolean,
    val tipoReporteID: Int,
    val esAnonimo: Boolean,
    val nombreUsuario: String,

    @SerializedName("Imagen")
    val imagenBase64: String?
)