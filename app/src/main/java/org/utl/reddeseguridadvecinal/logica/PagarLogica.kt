package org.utl.reddeseguridadvecinal.logica

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import org.utl.reddeseguridadvecinal.util.SessionManager

class PagarLogica {
    fun cerrarSesion(context: Context) {
        val sessionManager = SessionManager(context)

        sessionManager.clearSession()

        FirebaseAuth.getInstance().signOut()

        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
    }
}