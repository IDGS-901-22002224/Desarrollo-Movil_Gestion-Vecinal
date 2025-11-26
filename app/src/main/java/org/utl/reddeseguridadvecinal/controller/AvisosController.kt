package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.Aviso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Interface interna
interface AvisosService {
    @GET("api/Avisos")
    suspend fun getAvisos(): Response<List<Aviso>>
}

class AvisosController {

    private val avisosService: AvisosService = Api.createService(AvisosService::class.java)

    suspend fun obtenerAvisos(): List<Aviso> {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<List<Aviso>> = avisosService.getAvisos()
                if (response.isSuccessful) {
                    val todosLosAvisos = response.body() ?: emptyList()
                    // menos de 2 semanas
                    filtrarAvisosRecientes(todosLosAvisos)
                } else {
                    println("Error al obtener avisos: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun filtrarAvisosRecientes(avisos: List<Aviso>): List<Aviso> {
        val fechaActual = Calendar.getInstance().time
        val dosSemanasEnMillis = TimeUnit.DAYS.toMillis(14)

        return avisos.filter { aviso ->
            try {
                val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val fechaAviso = formatoFecha.parse(aviso.fechaPublicacion)

                if (fechaAviso != null) {
                    val diferencia = fechaActual.time - fechaAviso.time
                    diferencia <= dosSemanasEnMillis
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}