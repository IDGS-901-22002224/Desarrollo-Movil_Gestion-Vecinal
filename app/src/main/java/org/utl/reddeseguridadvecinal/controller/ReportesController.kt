package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.ReporteRequest
import org.utl.reddeseguridadvecinal.modelo.ReporteResponse
import org.utl.reddeseguridadvecinal.modelo.TipoReporteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ReportesApiService {
    @POST("api/Reportes")
    suspend fun crearReporte(@Body reporteRequest: ReporteRequest): Response<Unit>

    @GET("api/Reportes/usuario/{usuarioId}")
    suspend fun getReportesPorUsuario(@Path("usuarioId") usuarioId: Int): List<ReporteResponse>

    @GET("api/Reportes/tipos-reporte")
    suspend fun getTiposDeReporte(): List<TipoReporteResponse>

    @GET("api/Reportes")
    suspend fun getReportes(): List<ReporteResponse> // Pide TODOS los reportes
}

class ReportesController {
    private val apiService = Api.createService(ReportesApiService::class.java)

    suspend fun crearReporte(reporte: ReporteRequest): Boolean {
        return try {
            val response = apiService.crearReporte(reporte)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getReportesPorUsuario(usuarioId: Int): List<ReporteResponse>? {
        return try {
            apiService.getReportesPorUsuario(usuarioId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getTiposDeReporte(): List<TipoReporteResponse>? {
        return try {
            apiService.getTiposDeReporte()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getReportes(): List<ReporteResponse>? {
        return try {
            apiService.getReportes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}