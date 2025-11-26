package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.ApiResponse
import org.utl.reddeseguridadvecinal.modelo.UsuarioResponse
import org.utl.reddeseguridadvecinal.modelo.UsuarioUpdateRequest
import org.utl.reddeseguridadvecinal.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Path

interface UsuarioService {
    @GET("api/usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: Int): Response<UsuarioResponse>

    @PUT("api/usuarios/update")
    suspend fun updateUsuario(@Body updateRequest: UsuarioUpdateRequest): Response<ApiResponse>
}

class PerfilController(private val sessionManager: SessionManager) {

    private val usuarioService: UsuarioService = Api.createService(UsuarioService::class.java)

    suspend fun getUsuarioById(id: Int): UsuarioResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<UsuarioResponse> = usuarioService.getUsuarioById(id)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    println("Error al obtener usuario: ${response.code()} - ${response.message()}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun updateUsuario(updateRequest: UsuarioUpdateRequest): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                //println("respuesta de la actualizacion: $updateRequest")
                val response: Response<ApiResponse> = usuarioService.updateUsuario(updateRequest)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    //println("Respuesta exitosa: $apiResponse")
                    apiResponse?.mensaje != null || apiResponse?.message != null
                } else {
                    val errorBody = response.errorBody()?.string()
                    //println("Error en respuesta: ${response.code()} - ${response.message()}")
                    //println("Error body: $errorBody")
                    false
                }
            } catch (e: Exception) {
                //println("Excepcion en updateUsuario: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }
}