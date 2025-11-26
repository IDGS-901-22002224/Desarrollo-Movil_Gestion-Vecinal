package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.AlertaPanicoRequest
import org.utl.reddeseguridadvecinal.modelo.AlertaPanicoResponse
import retrofit2.http.Body
import retrofit2.http.POST

// Interface interna
interface AlertaPanicoApiService {
    @POST("api/Alertas")
    suspend fun crearAlerta(@Body request: AlertaPanicoRequest): AlertaPanicoResponse
}

class AlertaPanicoController {
    private val apiService = Api.createService(AlertaPanicoApiService::class.java)

    suspend fun crearAlertaPanico(usuarioID: Int, latitud: Double, longitud: Double): AlertaPanicoResponse? {
        return try {
            //println("creando alerta de panico para: $usuarioID")

            val alertaRequest = AlertaPanicoRequest(
                usuarioID = usuarioID,
                latitud = latitud,
                longitud = longitud
            )

            val response = apiService.crearAlerta(alertaRequest)
            //println("Alerta creada exitosamente: ${response.message}")
            response
        } catch (e: Exception) {
            //println("Error al crear alerta de panico: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}