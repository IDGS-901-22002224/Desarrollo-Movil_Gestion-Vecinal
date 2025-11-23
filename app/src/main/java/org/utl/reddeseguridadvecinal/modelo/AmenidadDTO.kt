
package org.utl.reddeseguridadvecinal.modelo

data class AmenidadDTO(
    val amenidadID: Int,
    val tipoAmenidadID: Int,
    val nombre: String,
    val ubicacion: String,
    val capacidad: Int,
    val activo: Boolean,
    val tipoAmenidadNombre: String,
    val horarioInicio: String,
    val horarioFin: String
)