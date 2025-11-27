package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.CargoServicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CargosServiciosService {
    @GET("api/Servicios/cargos/servicios/solicitud/{solicitudId}")
    suspend fun getCargosBySolicitud(@Path("solicitudId") solicitudId: Int): Response<List<CargoServicio>>
}

class CargosServiciosController {
    private val cargosService: CargosServiciosService = Api.createService(CargosServiciosService::class.java)

    suspend fun obtenerCargosPorSolicitud(solicitudId: Int): List<CargoServicio> {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<List<CargoServicio>> = cargosService.getCargosBySolicitud(solicitudId)
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    println("Error al obtener cargos: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}