package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.InvitadoRequest
import org.utl.reddeseguridadvecinal.modelo.InvitadoResponse
import org.utl.reddeseguridadvecinal.modelo.QRPersonalResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AccesosApiService {

    @GET("api/QRPersonal/usuario/{usuarioId}")
    suspend fun getQRPersonal(@Path("usuarioId") usuarioId: Int): QRPersonalResponse

    // Registrar Invitado
    @POST("api/Invitados")
    suspend fun crearInvitado(@Body request: InvitadoRequest): Response<Unit>

    // Historial de Invitados
    @GET("api/Invitados/usuario/{usuarioId}")
    suspend fun getHistorialInvitados(@Path("usuarioId") usuarioId: Int): List<InvitadoResponse>
}

class AccesosController {
    private val apiService = Api.createService(AccesosApiService::class.java)

    suspend fun getQRPersonal(usuarioId: Int): QRPersonalResponse? {
        return try {

            val qrEncontrado = apiService.getQRPersonal(usuarioId)

            println("ACCESO: QR Recibido: ${qrEncontrado.codigoQR}")

            if (qrEncontrado.activo) {
                qrEncontrado
            } else {
                println("ACCESO: El QR recibido no está activo.")
                null
            }

        } catch (e: Exception) {
            println("ACCESO ERROR: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun crearInvitado(invitado: InvitadoRequest): Boolean {
        return try {
            val response = apiService.crearInvitado(invitado)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getHistorialInvitados(usuarioId: Int): List<InvitadoResponse>? {
        return try {
            apiService.getHistorialInvitados(usuarioId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}