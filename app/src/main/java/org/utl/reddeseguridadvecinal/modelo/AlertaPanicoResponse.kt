package org.utl.reddeseguridadvecinal.modelo

data class AlertaPanicoResponse(
    val message: String,
    val alertaId: Int,
    val firebaseId: String
)