
package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.SolicitudServicioRequest
import org.utl.reddeseguridadvecinal.modelo.TipoServicioDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ServiciosService {
    @GET("api/Servicios/tipos-servicio")
    suspend fun getTiposServicio(): Response<List<TipoServicioDTO>>

    @POST("api/Servicios/solicitud")
    suspend fun createSolicitud(@Body request: SolicitudServicioRequest): Response<Any>
}

class ServiciosController {

    private val serviciosService: ServiciosService = Api.createService(ServiciosService::class.java)

    suspend fun obtenerTiposServicio(): List<TipoServicioDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<List<TipoServicioDTO>> = serviciosService.getTiposServicio()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    println("Error al obtener tipos de servicio: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun crearSolicitud(request: SolicitudServicioRequest): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("Enviando solicitud: $request")
                val response: Response<Any> = serviciosService.createSolicitud(request)
                println("Respuesta: ${response.code()} - ${response.message()}")
                if (response.isSuccessful) {
                    println("Solicitud creada exitosamente")
                    true
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("Error al crear solicitud: ${response.code()} - ${response.message()} - $errorBody")
                    false
                }
            } catch (e: Exception) {
                println("Excepción al crear solicitud: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }
}