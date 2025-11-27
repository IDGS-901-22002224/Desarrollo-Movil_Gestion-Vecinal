package org.utl.reddeseguridadvecinal.modelo

data class AbonoParcialRequest(
    val usuarioId: Int,
    val cargosMantenimiento: List<CargoAbonoDTO>,
    val cargosServicios: List<CargoAbonoDTO>,
    val montoTotal: Double,
    val fechaAbono: String
)