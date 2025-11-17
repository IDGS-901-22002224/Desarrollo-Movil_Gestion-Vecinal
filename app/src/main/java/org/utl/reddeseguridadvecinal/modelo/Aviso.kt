package org.utl.reddeseguridadvecinal.modelo

data class Aviso(
    val avisoID: Int,
    val usuarioID: Int,
    val categoriaID: Int,
    val titulo: String,
    val descripcion: String,
    val fechaEvento: String,
    val fechaPublicacion: String,
    val categoriaNombre: String,
    val categoriaActiva: Boolean
)