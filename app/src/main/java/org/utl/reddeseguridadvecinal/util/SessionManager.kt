package org.utl.reddeseguridadvecinal.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var preferences: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_APELLIDO_P = "apellido_p"
        private const val KEY_APELLIDO_M = "apellido_m"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_NUMERO_CASA = "numero_casa"
        private const val KEY_CALLE = "calle"
        private const val KEY_FIREBASE_ID = "firebase_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
    }

    fun saveUserSession(
        userId: Int,
        userName: String,
        apellidoP: String,
        apellidoM: String,
        userType: String,
        numeroCasa: String,
        calle: String,
        firebaseId: String? = null
    ) {
        preferences.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_APELLIDO_P, apellidoP)
            putString(KEY_APELLIDO_M, apellidoM)
            putString(KEY_USER_TYPE, userType)
            putString(KEY_NUMERO_CASA, numeroCasa)
            putString(KEY_CALLE, calle)
            putString(KEY_FIREBASE_ID, firebaseId ?: "")
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    fun getUserId(): Int = preferences.getInt(KEY_USER_ID, -1)
    fun getUserName(): String = preferences.getString(KEY_USER_NAME, "") ?: ""
    fun getApellidoP(): String = preferences.getString(KEY_APELLIDO_P, "") ?: ""
    fun getApellidoM(): String = preferences.getString(KEY_APELLIDO_M, "") ?: ""
    fun getUserType(): String = preferences.getString(KEY_USER_TYPE, "") ?: ""
    fun getNumeroCasa(): String = preferences.getString(KEY_NUMERO_CASA, "") ?: ""
    fun getCalle(): String = preferences.getString(KEY_CALLE, "") ?: ""
    fun getFirebaseId(): String = preferences.getString(KEY_FIREBASE_ID, "") ?: ""

    fun getApellidosCompletos(): String {
        val apellidoP = getApellidoP()
        val apellidoM = getApellidoM()
        return "$apellidoP $apellidoM".trim()
    }

    // Direccion
    fun getDireccionCompleta(): String {
        val numeroCasa = getNumeroCasa()
        val calle = getCalle()

        return if (calle.isNotEmpty() && numeroCasa.isNotEmpty()) {
            "Casa #$numeroCasa, $calle"
        } else if (numeroCasa.isNotEmpty()) {
            "Casa #$numeroCasa"
        } else if (calle.isNotEmpty()) {
            calle
        } else {
            "Casa #000" // por defecto
        }
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false)

        if (loggedIn) {
            val loginTime = preferences.getLong(KEY_LOGIN_TIMESTAMP, 0)
            val currentTime = System.currentTimeMillis()
            val thirtyDaysInMillis = 30L * 24L * 60L * 60L * 1000L

            if (currentTime - loginTime > thirtyDaysInMillis) {
                clearSession()
                return false
            }
        }

        return loggedIn
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }
}