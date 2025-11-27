package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.ReservaDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HistorialReservas {
    @GET("api/Reservas/usuario/{usuarioId}")
    suspend fun getReservasByUsuario(@Path("usuarioId") usuarioId: Int): Response<List<ReservaDTO>>
}

class ReservasController {

    private val historialReservas: HistorialReservas = Api.createService(HistorialReservas::class.java)

    suspend fun obtenerReservasPorUsuario(usuarioId: Int): List<ReservaDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<List<ReservaDTO>> = historialReservas.getReservasByUsuario(usuarioId)
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    println("Error al obtener reservas: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}