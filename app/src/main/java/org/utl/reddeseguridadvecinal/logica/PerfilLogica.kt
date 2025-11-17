package org.utl.reddeseguridadvecinal.controller

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import org.utl.reddeseguridadvecinal.modelo.UsuarioUpdateRequest
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.util.Calendar

class PerfilLogica(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val perfilController: PerfilController
) {

    fun prepararDatosActualizacion(
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        numeroCasa: String,
        calle: String,
        telefono: String,
        fechaNacimiento: String,
        email: String,
        password: String,
        numeroTarjeta: String,
        fechaVencimiento: String
    ): UsuarioUpdateRequest? {

        val usuarioId = sessionManager.getUserId()
        if (usuarioId == -1) {
            Toast.makeText(context, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return null
        }

        // Limpiar los formatos
        val telefonoLimpio = telefono.replace("-", "").trim()
        val emailLimpio = email.trim()
        val fechaNacimientoLimpia = fechaNacimiento.replace("-", "").trim()
        val fechaVencimientoInput = fechaVencimiento.trim()
        val numeroTarjetaLimpio = numeroTarjeta.replace("-", "").replace(" ", "").trim()
/*
        println("FechaVencimientoInput: '$fechaVencimientoInput'")
        println("Telefono: '$telefonoLimpio'")
        println("Email: '$emailLimpio'")
        println("FechaNacimiento: '$fechaNacimientoLimpia'")
        println("NumeroTarjeta: '$numeroTarjetaLimpio'")
*/
        // Validar campos
        if (telefonoLimpio.isEmpty() || emailLimpio.isEmpty() || fechaNacimientoLimpia.isEmpty()) {
            Toast.makeText(context, "Complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
            return null
        }

        // Validar formato de fecha de nacimiento
        if (fechaNacimientoLimpia.length != 8) {
            Toast.makeText(context, "Formato de fecha de nacimiento inválido. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            return null
        }

        // Validar que la tarjeta tenga 16 dígitos
        if (numeroTarjetaLimpio.isNotEmpty() && numeroTarjetaLimpio.length != 16) {
            Toast.makeText(context, "El número de tarjeta debe tener 16 dígitos", Toast.LENGTH_SHORT).show()
            return null
        }

        // Validar fecha de vencimiento
        if (fechaVencimientoInput.isNotEmpty()) {
            if (!isValidExpiryDate(fechaVencimientoInput)) {

                return null
            }
        }

        // Validar email
        if (!isValidEmail(emailLimpio)) {
            Toast.makeText(context, "Formato de email inválido", Toast.LENGTH_SHORT).show()
            return null
        }

        // Validar telefono
        if (telefonoLimpio.length != 10) {
            Toast.makeText(context, "El teléfono debe tener 10 dígitos", Toast.LENGTH_SHORT).show()
            return null
        }

        // Formatear fecha de nacimiento(YYYY-MM-DD)
        val fechaNacimientoFormateada = "${fechaNacimientoLimpia.substring(0, 4)}-${fechaNacimientoLimpia.substring(4, 6)}-${fechaNacimientoLimpia.substring(6, 8)}"

        // Formatear fecha de vencimiento(MM/YY)
        val fechaVencimientoFormateada = if (fechaVencimientoInput.isNotEmpty()) {
            val cleanFechaVencimiento = fechaVencimientoInput.replace("-", "")
            "${cleanFechaVencimiento.substring(0, 2)}/${cleanFechaVencimiento.substring(2, 4)}"
        } else {
            ""
        }

        // ultimos digitos de tarjeta
        val ultimosDigitos = if (numeroTarjetaLimpio.length >= 4) {
            numeroTarjetaLimpio.takeLast(4)
        } else {
            numeroTarjetaLimpio
        }

        // PaRa la contraseña
        val passwordLimpia = password.trim()

        // objeto para enviar a la API
        return UsuarioUpdateRequest(
            usuarioID = usuarioId,
            nombre = nombre.trim(),
            apellidoPaterno = apellidoPaterno.trim(),
            apellidoMaterno = apellidoMaterno.trim(),
            numeroCasa = numeroCasa.trim(),
            calle = calle.trim(),
            telefono = telefonoLimpio,
            fechaNacimiento = fechaNacimientoFormateada,
            email = emailLimpio,
            password = passwordLimpia,
            numeroTarjeta = numeroTarjetaLimpio,
            ultimosDigitos = ultimosDigitos,
            fechaVencimiento = fechaVencimientoFormateada
        )
    }

    fun formatTelefono(telefono: String): String {
        if (telefono.isEmpty()) return ""
        val clean = telefono.replace("-", "")
        return when {
            clean.length >= 10 -> "${clean.substring(0, 3)}-${clean.substring(3, 6)}-${clean.substring(6)}"
            clean.length >= 6 -> "${clean.substring(0, 3)}-${clean.substring(3)}"
            else -> telefono
        }
    }

    fun formatFechaNacimiento(fecha: String): String {
        if (fecha.isEmpty()) return ""
        val clean = fecha.replace("-", "")
        return when {
            clean.length >= 8 -> "${clean.substring(0, 4)}-${clean.substring(4, 6)}-${clean.substring(6, 8)}"
            else -> fecha
        }
    }

    fun formatFechaVencimiento(fecha: String): String {
        if (fecha.isEmpty()) return ""
        val clean = fecha.replace("/", "").replace("-", "")
        return when {
            clean.length >= 4 -> "${clean.substring(0, 2)}-${clean.substring(2, 4)}"
            else -> fecha
        }
    }

    // TextWatchers
    fun configurarTextWatcherTelefono(editText: EditText) {
        editText.addTextChangedListener(createPhoneTextWatcher(editText))
    }

    fun configurarTextWatcherFechaNacimiento(editText: EditText) {
        editText.addTextChangedListener(createBirthDateTextWatcher(editText))
    }

    fun configurarTextWatcherFechaVencimiento(editText: EditText) {
        editText.addTextChangedListener(createExpiryDateTextWatcher(editText))
    }

    fun configurarTextWatcherNumeroTarjeta(editText: EditText) {
        editText.addTextChangedListener(createCardNumberTextWatcher(editText))
    }

    // TextWatcher telefono (477-123-1234)
    private fun createPhoneTextWatcher(editText: EditText): TextWatcher {
        return object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s.isNullOrEmpty()) return
                isFormatting = true

                try {
                    val text = s.toString()
                    val cleanText = text.replace("-", "")

                    if (text == getFormattedPhone(cleanText)) {
                        return
                    }

                    when {
                        cleanText.length <= 10 -> {
                            val formatted = getFormattedPhone(cleanText)
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        }
                        else -> {
                            val limited = cleanText.substring(0, 10)
                            val formatted = getFormattedPhone(limited)
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        }
                    }
                } finally {
                    isFormatting = false
                }
            }

            private fun getFormattedPhone(cleanText: String): String {
                return when (cleanText.length) {
                    in 1..3 -> cleanText
                    in 4..6 -> "${cleanText.substring(0, 3)}-${cleanText.substring(3)}"
                    in 7..10 -> "${cleanText.substring(0, 3)}-${cleanText.substring(3, 6)}-${cleanText.substring(6)}"
                    else -> cleanText.substring(0, 10).let {
                        "${it.substring(0, 3)}-${it.substring(3, 6)}-${it.substring(6)}"
                    }
                }
            }
        }
    }

    // TextWatcher  fecha de nacimiento (2004-08-02)
    private fun createBirthDateTextWatcher(editText: EditText): TextWatcher {
        return object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s.isNullOrEmpty()) return
                isFormatting = true

                try {
                    val text = s.toString()
                    val cleanText = text.replace("-", "")

                    if (text == getFormattedBirthDate(cleanText)) {
                        return
                    }

                    when {
                        cleanText.length <= 8 -> {
                            val formatted = getFormattedBirthDate(cleanText)
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        }
                        else -> {
                            val limited = cleanText.substring(0, 8)
                            val formatted = getFormattedBirthDate(limited)
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        }
                    }
                } finally {
                    isFormatting = false
                }
            }

            private fun getFormattedBirthDate(cleanText: String): String {
                return when (cleanText.length) {
                    in 1..4 -> cleanText
                    in 5..6 -> "${cleanText.substring(0, 4)}-${cleanText.substring(4)}"
                    in 7..8 -> "${cleanText.substring(0, 4)}-${cleanText.substring(4, 6)}-${cleanText.substring(6)}"
                    else -> cleanText.substring(0, 8).let {
                        "${it.substring(0, 4)}-${it.substring(4, 6)}-${it.substring(6)}"
                    }
                }
            }
        }
    }

    // TextWatcher fecha de vencimiento (MM-YY)
    private fun createExpiryDateTextWatcher(editText: EditText): TextWatcher {
        return object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s.isNullOrEmpty()) return
                isFormatting = true

                try {
                    val text = s.toString()
                    val cleanText = text.replace("-", "")

                    if (text == getFormattedExpiryDate(cleanText)) {
                        return
                    }

                    when {
                        cleanText.length <= 4 -> {
                            val formatted = getFormattedExpiryDate(cleanText)
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        }
                        else -> {
                            val limited = cleanText.substring(0, 4)
                            val formatted = getFormattedExpiryDate(limited)
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        }
                    }
                } finally {
                    isFormatting = false
                }
            }

            private fun getFormattedExpiryDate(cleanText: String): String {
                return when (cleanText.length) {
                    in 1..2 -> cleanText
                    in 3..4 -> "${cleanText.substring(0, 2)}-${cleanText.substring(2)}"
                    else -> cleanText.substring(0, 4).let {
                        "${it.substring(0, 2)}-${it.substring(2)}"
                    }
                }
            }
        }
    }

    // TextWatcher numero de tarjeta (1234-1234-1234-1234)
    private fun createCardNumberTextWatcher(editText: EditText): TextWatcher {
        return object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s.isNullOrEmpty()) return
                isFormatting = true

                try {
                    val text = s.toString()
                    val cleanText = text.replace("-", "")

                    if (text == getFormattedCardNumber(cleanText)) {
                        return
                    }

                    when {
                        cleanText.length <= 16 -> {
                            val formatted = getFormattedCardNumber(cleanText)
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        }
                        else -> {
                            val limited = cleanText.substring(0, 16)
                            val formatted = getFormattedCardNumber(limited)
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        }
                    }
                } finally {
                    isFormatting = false
                }
            }

            private fun getFormattedCardNumber(cleanText: String): String {
                return when (cleanText.length) {
                    in 1..4 -> cleanText
                    in 5..8 -> "${cleanText.substring(0, 4)}-${cleanText.substring(4)}"
                    in 9..12 -> "${cleanText.substring(0, 4)}-${cleanText.substring(4, 8)}-${cleanText.substring(8)}"
                    in 13..16 -> "${cleanText.substring(0, 4)}-${cleanText.substring(4, 8)}-${cleanText.substring(8, 12)}-${cleanText.substring(12)}"
                    else -> cleanText.substring(0, 16).let {
                        "${it.substring(0, 4)}-${it.substring(4, 8)}-${it.substring(8, 12)}-${it.substring(12)}"
                    }
                }
            }
        }
    }

    // Validacion fecha de vencimiento
    private fun isValidExpiryDate(date: String): Boolean {
        val pattern = Regex("^(0[1-9]|1[0-2])-([0-9]{2})\$")
        if (!pattern.matches(date)) {
            Toast.makeText(context, "Formato de fecha de vencimiento inválido. Use MM-YY", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val parts = date.split("-")
            val month = parts[0].toInt()
            val year = parts[1].toInt()

            // fecha actual
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR) % 100
            val currentMonth = calendar.get(Calendar.MONTH) + 1

            // no sea una fecha pasada
            val isValid = when {
                year > currentYear -> true
                year == currentYear -> month >= currentMonth
                else -> false
            }

            if (!isValid) {
                Toast.makeText(context, "La tarjeta está expirada", Toast.LENGTH_SHORT).show()
            }

            isValid
        } catch (e: Exception) {
            Toast.makeText(context, "Error en formato de fecha de vencimiento", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Funcion para validar email
    private fun isValidEmail(email: String): Boolean {
        val pattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        return pattern.matches(email)
    }

    fun cerrarSesion() {
        val sessionManager = SessionManager(context)

        sessionManager.clearSession()

        FirebaseAuth.getInstance().signOut()

        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
    }
}