package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.SolicitudServicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HistorialServiciosService {
    @GET("api/Servicios/solicitud/usuario/{usuarioId}")
    suspend fun getSolicitudesByUsuario(@Path("usuarioId") usuarioId: Int): Response<List<SolicitudServicio>>
}

class HistorialServiciosController {

    private val historialServiciosService: HistorialServiciosService = Api.createService(HistorialServiciosService::class.java)

    suspend fun obtenerSolicitudesPorUsuario(usuarioId: Int): List<SolicitudServicio> {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<List<SolicitudServicio>> = historialServiciosService.getSolicitudesByUsuario(usuarioId)
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    println("Error al obtener solicitudes: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}