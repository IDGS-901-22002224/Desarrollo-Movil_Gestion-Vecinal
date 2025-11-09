package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etUsuario: EditText
    private lateinit var etContrasena: EditText
    private lateinit var scrollView: ScrollView
    private var isKeyboardVisible = false

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val rootLayout = findViewById<View>(R.id.main)
        scrollView = rootLayout.parent as ScrollView

        // Deshabilitar scroll táctil del usuario
        scrollView.setOnTouchListener { _, _ ->
            !isKeyboardVisible // Bloquear scroll cuando NO hay teclado
        }

        rootLayout.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        // Detectar cuando el teclado está visible
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootLayout.rootView.height - rootLayout.height
            isKeyboardVisible = heightDiff > 200 // El teclado está visible

            if (!isKeyboardVisible) {
                // Cuando se oculta el teclado, volver arriba
                scrollView.smoothScrollTo(0, 0)
            }
        }

        // INICIALIZA FIREBASE AUTH
        auth = FirebaseAuth.getInstance()

        // ENLAZAR CAMPOS DE TEXTO
        etUsuario = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)

        // Configurar listeners para hacer scroll automático solo cuando hay teclado
        etContrasena.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                scrollView.postDelayed({
                    scrollView.smoothScrollTo(0, view.bottom)
                }, 300)
            }
        }

        etUsuario.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollView.postDelayed({
                    scrollView.smoothScrollTo(0, 0)
                }, 300)
            }
        }

        setupLoginButton()
    }

    private fun setupLoginButton() {
        val btnLogin = findViewById<CardView>(R.id.btnIniciarSesion)

        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim()
            val password = etContrasena.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Acceso concedido.", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, Home::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}