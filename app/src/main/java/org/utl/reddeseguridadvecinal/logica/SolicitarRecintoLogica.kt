
package org.utl.reddeseguridadvecinal.logica

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.AmenidadesController
import org.utl.reddeseguridadvecinal.controller.SolicitarRecintoController
import org.utl.reddeseguridadvecinal.modelo.AmenidadDTO
import org.utl.reddeseguridadvecinal.modelo.CreateReservaRequest
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SolicitarRecintoLogica {

    private val amenidadesController = AmenidadesController()
    private val solicitarRecintoController = SolicitarRecintoController()
    private val calendar = Calendar.getInstance()
    var amenidadesList: List<AmenidadDTO> = emptyList()

    fun cerrarSesion(context: Context) {
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
    }

    fun cargarAmenidades(
        context: Context,
        onSuccess: (List<AmenidadDTO>) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                amenidadesList = amenidadesController.obtenerAmenidades()
                if (amenidadesList.isNotEmpty()) {
                    onSuccess(amenidadesList)
                } else {
                    onError("No hay recintos disponibles")
                }
            } catch (e: Exception) {
                onError("Error al cargar recintos: ${e.message}")
            }
        }
    }

    fun mostrarDatePicker(context: Context, editText: EditText) {
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                actualizarFechaEditText(editText)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    fun mostrarTimePicker(context: Context, editText: EditText) {
        val timePicker = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                actualizarHoraEditText(editText)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }

    private fun actualizarFechaEditText(editText: EditText) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        editText.setText(dateFormat.format(calendar.time))
    }

    private fun actualizarHoraEditText(editText: EditText) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        editText.setText(timeFormat.format(calendar.time))
    }

    fun validarYGuardarReserva(
        context: Context,
        usuarioId: Int,
        spinnerRecinto: Spinner,
        detalles: String,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Validar usuario
        if (usuarioId == -1) {
            onError("Error: No se pudo identificar al usuario")
            return
        }

        // Validar selección de recinto
        if (spinnerRecinto.selectedItemPosition == -1) {
            onError("Por favor seleccione un recinto")
            return
        }

        // Validar detalles
        if (detalles.isEmpty()) {
            onError("Por favor ingrese los detalles del evento")
            return
        }

        // Validar fecha
        if (fecha.isEmpty()) {
            onError("Por favor seleccione una fecha")
            return
        }

        // Validar horas
        if (horaInicio.isEmpty() || horaFin.isEmpty()) {
            onError("Por favor seleccione ambas horas")
            return
        }

        // Obtener amenidad seleccionada
        val selectedAmenidad = amenidadesList[spinnerRecinto.selectedItemPosition]

        // Crear request
        val reservaRequest = CreateReservaRequest(
            usuarioID = usuarioId,
            amenidadID = selectedAmenidad.amenidadID,
            fechaReserva = convertirFechaFormato(fecha),
            horaInicio = horaInicio,
            horaFin = horaFin,
            motivo = detalles
        )

        // Enviar reserva
        enviarReserva(context, reservaRequest, onSuccess, onError)
    }

    private fun convertirFechaFormato(fecha: String): String {
        return try {
            // Convertir de dd/MM/yyyy a yyyy-MM-dd
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(fecha)
            outputFormat.format(date)
        } catch (e: Exception) {
            fecha // Si hay error, devolver la fecha original
        }
    }

    private fun enviarReserva(
        context: Context,
        reservaRequest: CreateReservaRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val exito = solicitarRecintoController.crearReserva(reservaRequest)

                if (exito) {
                    Toast.makeText(context, "Reserva solicitada exitosamente", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    onError("Error al crear la reserva")
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.message}")
            }
        }
    }
}