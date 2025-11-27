
package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.AmenidadDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET

interface AmenidadesService {
    @GET("api/Amenidades")
    suspend fun getAmenidades(): Response<List<AmenidadDTO>>
}

class AmenidadesController {

    private val amenidadesService: AmenidadesService = Api.createService(AmenidadesService::class.java)

    suspend fun obtenerAmenidades(): List<AmenidadDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<List<AmenidadDTO>> = amenidadesService.getAmenidades()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    println("Error al obtener amenidades: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}