package org.utl.reddeseguridadvecinal.modelo

import com.google.gson.annotations.SerializedName

data class QRPersonalResponse(

    @SerializedName("qrid", alternate = ["QRID", "qrID", "QrId"])
    val qrId: Int,

    @SerializedName("codigoQR", alternate = ["CodigoQR", "CodigoQr"])
    val codigoQR: String,

    @SerializedName("fechaVencimiento", alternate = ["FechaVencimiento"])
    val fechaVencimiento: String,

    @SerializedName("activo", alternate = ["Activo"])
    val activo: Boolean
)

data class InvitadoRequest(
    val usuarioID: Int,
    val nombreInvitado: String,
    val apellidoPaternoInvitado: String,
    val apellidoMaternoInvitado: String,
    val fechaVencimiento: String
)

data class InvitadoResponse(
    val invitadoID: Int,
    val nombreInvitado: String,
    val apellidoPaternoInvitado: String,
    val fechaGeneracion: String,
    val fechaEntrada: String?,
    val fechaSalida: String?,
    val codigoQR: String,
    val activo: Boolean
)