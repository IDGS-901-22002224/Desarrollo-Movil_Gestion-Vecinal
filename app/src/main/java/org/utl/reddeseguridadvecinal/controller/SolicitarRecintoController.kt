
package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.CreateReservaRequest
import org.utl.reddeseguridadvecinal.modelo.ReservaResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReservasService {
    @POST("api/Reservas")
    suspend fun createReserva(@Body request: CreateReservaRequest): Response<ReservaResponse>
}

class SolicitarRecintoController {

    private val reservasService: ReservasService = Api.createService(ReservasService::class.java)

    suspend fun crearReserva(request: CreateReservaRequest): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<ReservaResponse> = reservasService.createReserva(request)
                if (response.isSuccessful) {
                    true
                } else {
                    println("Error al crear reserva: ${response.code()} - ${response.message()}")
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}