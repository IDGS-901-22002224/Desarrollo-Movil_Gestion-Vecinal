package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.MarcadorResponse
import retrofit2.http.GET

// Interface para la API de Mapa
interface MapaApiService {
    @GET("api/Mapa/marcadores")
    suspend fun getMarcadores(): List<MarcadorResponse> // Esperamos una lista de marcadores
}

// Controller del Mapa
class MapaController {
    private val apiService = Api.createService(MapaApiService::class.java)

    suspend fun getMarcadores(): List<MarcadorResponse>? {
        return try {
            apiService.getMarcadores()
        } catch (e: Exception) {
            e.printStackTrace()
            null // Devuelve nulo si hay un error de red
        }
    }
}