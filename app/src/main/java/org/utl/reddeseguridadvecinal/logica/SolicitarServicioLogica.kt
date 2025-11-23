package org.utl.reddeseguridadvecinal.logica

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.ServiciosController
import org.utl.reddeseguridadvecinal.modelo.SolicitudServicioRequest
import org.utl.reddeseguridadvecinal.modelo.TipoServicioDTO
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SolicitarServicioLogica {

    private val serviciosController = ServiciosController()
    private val calendar = Calendar.getInstance()

    // Lista de niveles de urgencia
    val nivelesUrgencia = listOf("Baja", "Media", "Alta")

    fun cerrarSesion(context: Context) {
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
    }

    fun cargarServiciosEnSpinner(
        context: Context,
        onServiciosCargados: (List<TipoServicioDTO>) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val servicios = serviciosController.obtenerTiposServicio()
                if (servicios.isNotEmpty()) {
                    onServiciosCargados(servicios)
                } else {
                    onError("No hay servicios disponibles")
                }
            } catch (e: Exception) {
                onError("Error al cargar servicios")
                e.printStackTrace()
            }
        }
    }

    fun crearAdaptadorSpinner(
        context: Context,
        servicios: List<TipoServicioDTO>
    ): ArrayAdapter<TipoServicioDTO> {
        return object : ArrayAdapter<TipoServicioDTO>(context, android.R.layout.simple_spinner_item, servicios) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return crearVista(position, convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return crearVista(position, convertView, parent)
            }

            private fun crearVista(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: android.view.LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)

                val textView = view.findViewById<TextView>(android.R.id.text1)
                val servicio = servicios[position]

                textView.text = servicio.nombre
                textView.setTextColor(0xFF111827.toInt())

                return view
            }
        }
    }

    fun crearAdaptadorUrgencia(context: Context): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, nivelesUrgencia) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return crearVistaUrgencia(position, convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return crearVistaUrgencia(position, convertView, parent)
            }

            private fun crearVistaUrgencia(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: android.view.LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)

                val textView = view.findViewById<TextView>(android.R.id.text1)
                val urgencia = nivelesUrgencia[position]

                textView.text = urgencia
                textView.setTextColor(0xFF111827.toInt())

                return view
            }
        }
    }

    fun mostrarDatePicker(context: Context, onDateSelected: (String) -> Unit) {
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                val fechaFormateada = formatearFechaAPI(calendar.timeInMillis) // Cambiado a formato API
                onDateSelected(fechaFormateada)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    fun mostrarTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
        val timePicker = TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                val horaFormateada = formatearHora(calendar.timeInMillis)
                onTimeSelected(horaFormateada)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }

    fun validarFormulario(
        servicioSeleccionado: TipoServicioDTO?,
        detalles: String,
        urgencia: String?,
        fecha: String,
        hora: String
    ): String? {
        return when {
            servicioSeleccionado == null -> "Seleccione un servicio"
            detalles.trim().isEmpty() -> "Ingrese los detalles del servicio"
            urgencia == null -> "Seleccione el nivel de urgencia"
            fecha.isEmpty() -> "Seleccione una fecha"
            hora.isEmpty() -> "Seleccione una hora"
            else -> null
        }
    }

    fun enviarSolicitud(
        context: Context,
        usuarioID: Int,
        servicioSeleccionado: TipoServicioDTO,
        detalles: String,
        urgencia: String,
        fecha: String,
        hora: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Convertir fecha de DD/MM/YYYY a YYYY-MM-DD para la API
        val fechaAPI = convertirFechaParaAPI(fecha)

        val solicitud = SolicitudServicioRequest(
            usuarioID = usuarioID,
            tipoServicioID = servicioSeleccionado.tipoServicioID,
            descripcion = detalles,
            urgencia = urgencia,
            fechaPreferida = fechaAPI, // Usar el formato correcto para API
            horaPreferida = hora
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val exito = serviciosController.crearSolicitud(solicitud)
                if (exito) {
                    onSuccess()
                } else {
                    onError("Error al crear la solicitud")
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun formatearFecha(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(timestamp)
    }

    private fun formatearFechaAPI(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(timestamp)
    }

    private fun formatearHora(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(timestamp)
    }

    private fun convertirFechaParaAPI(fecha: String): String {
        return try {
            // Si la fecha viene en formato DD/MM/YYYY, convertir a YYYY-MM-DD
            if (fecha.contains("/")) {
                val partes = fecha.split("/")
                if (partes.size == 3) {
                    "${partes[2]}-${partes[1]}-${partes[0]}"
                } else {
                    fecha
                }
            } else {
                fecha
            }
        } catch (e: Exception) {
            fecha
        }
    }
}