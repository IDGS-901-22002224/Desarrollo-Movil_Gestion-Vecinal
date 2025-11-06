package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView // Importa TextView si fuera necesario para el botón
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // ¡Importante! El botón es un CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Aplica el padding para barras del sistema (boilerplate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar la redirección del botón de inicio de sesión
        setupLoginButton()
    }

    private fun setupLoginButton() {
        // 1. Obtener la referencia al botón (que es un CardView en tu XML)
        val btnLogin = findViewById<CardView>(R.id.btnIniciarSesion)

        // 2. Configurar el Listener
        btnLogin.setOnClickListener {
            // Lógica para intentar iniciar sesión iría aquí (validación de campos, API call, etc.)

            // Suponiendo que el inicio de sesión es exitoso, creamos el Intent:
            val intent = Intent(this, Home::class.java)

            // Opcional: Esto evita que el usuario pueda volver a la pantalla de Login
            // al presionar el botón 'Atrás' desde la pantalla Home.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Iniciar la actividad Home
            startActivity(intent)

            // Opcional: Finalizar la actividad de Login
            finish()
        }
    }
}