package org.utl.reddeseguridadvecinal.logica

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.Login
import org.utl.reddeseguridadvecinal.controller.AlertaPanicoController
import org.utl.reddeseguridadvecinal.util.SessionManager

class HomeLogica(private val context: Context) {

    private val sessionManager = SessionManager(context)
    private val alertaController = AlertaPanicoController()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun performLogout() {
        sessionManager.clearSession()

        FirebaseAuth.getInstance().signOut()

        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

        val intent = Intent(context, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    fun isUserLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun getApellidosCompletos(): String {
        return sessionManager.getApellidosCompletos()
    }

    fun getDireccionCompleta(): String {
        return sessionManager.getDireccionCompleta()
    }

    fun getUserId(): Int {
        return sessionManager.getUserId()
    }

    fun redirectToLoginIfNotLoggedIn(): Boolean {
        if (!isUserLoggedIn()) {
            val intent = Intent(context, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            return true
        }
        return false
    }

    // Metodo para la alerta de panico
    fun manejarAlertaPanico(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        // Verificar permisos de ubicacion
        if (!tienePermisosUbicacion()) {
            onError("Se necesitan permisos de ubicación para enviar alertas de panico")
            return
        }

        // Verificar si la ubicacion está activada
        if (!estaUbicacionActiva()) {
            onError("Por favor activa la ubicación para enviar alertas de pánico")
            return
        }

        // ubicacion actual
        obtenerUbicacionActual { location ->
            if (location != null) {
                // Enviar al servidor
                enviarAlertaAlServidor(location.latitude, location.longitude, onSuccess, onError)
            } else {
                onError("No se pudo obtener la ubicación actual")
            }
        }
    }

    private fun tienePermisosUbicacion(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun estaUbicacionActiva(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun obtenerUbicacionActual(onLocationObtained: (Location?) -> Unit) {
        try {
            if (tienePermisosUbicacion()) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        onLocationObtained(location)
                    }
                    .addOnFailureListener { exception ->
                        //println("Error al obtener ubicacion: ${exception.message}")
                        onLocationObtained(null)
                    }
            } else {
                onLocationObtained(null)
            }
        } catch (e: SecurityException) {
            //println("Excepcion de seguridad al obtener ubicación: ${e.message}")
            onLocationObtained(null)
        }
    }

    private fun enviarAlertaAlServidor(
        latitud: Double,
        longitud: Double,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioID = getUserId()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = alertaController.crearAlertaPanico(usuarioID, latitud, longitud)

                CoroutineScope(Dispatchers.Main).launch {
                    if (response != null) {
                        onSuccess("Alerta de pánico enviada exitosamente")
                    } else {
                        onError("Error al enviar la alerta de pánico")
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onError("Error de conexión: ${e.message}")
                }
            }
        }
    }
}