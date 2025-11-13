package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.LoginRequest
import org.utl.reddeseguridadvecinal.modelo.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

// Interface para la API
interface LoginApiService {
    @POST("api/Usuarios/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse
}

class LoginController {
    private val apiService = Api.createService(LoginApiService::class.java)

    suspend fun loginUser(email: String, password: String): LoginResponse? {
        return try {
            //println("Intentando login con: $email")

            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)

            //VALIDAR QUE NO SEA GUARDIA
            if (response.tipoUsuario.equals("Guardia", ignoreCase = true)) {
                //println("Acceso denegado a guardia")
                return null
            }

            //println("Login exitoso")
            response
        } catch (e: Exception) {
            //println(" Error en login: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
