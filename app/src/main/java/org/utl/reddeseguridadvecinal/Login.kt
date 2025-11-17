package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.app.Dialog
import android.graphics.Color
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.LoginController
import org.utl.reddeseguridadvecinal.util.SessionManager

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etUsuario: EditText
    private lateinit var etContrasena: EditText
    private lateinit var scrollView: ScrollView
    private var isKeyboardVisible = false

    private val loginController = LoginController()

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // VERIFICAR SESION ACTIVA
        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.getUserType() != "usuario_firebase") {
                val intent = Intent(this, Home::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                return
            } else {
                sessionManager.clearSession()
                Toast.makeText(this, "Sesión anterior invalida. Ingresa nuevamente.", Toast.LENGTH_LONG).show()
            }
        }

        setContentView(R.layout.activity_login)

        val rootLayout = findViewById<View>(R.id.main)
        scrollView = rootLayout.parent as ScrollView

        // Deshabilitar scroll tactil del usuario
        scrollView.setOnTouchListener { _, _ ->
            !isKeyboardVisible // Bloquear scroll cuando NO hay teclado
        }

        rootLayout.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        // Detectar cuando el teclado esta visible
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootLayout.rootView.height - rootLayout.height
            isKeyboardVisible = heightDiff > 200

            if (!isKeyboardVisible) {
                scrollView.smoothScrollTo(0, 0)
            }
        }

        // INICIALIZA FIREBASE AUTH
        auth = FirebaseAuth.getInstance()

        etUsuario = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)

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
        setupSoporteTecnico()
    }

    private fun setupLoginButton() {
        val btnLogin = findViewById<CardView>(R.id.btnIniciarSesion)

        // INICIALIZAR SessionManager
        val sessionManager = SessionManager(this)

        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim()
            val password = etContrasena.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar loading
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                // println("Intentando login solo con API")

                val usuario = loginController.loginUser(email, password)

                if (usuario != null) {
                    // SI SALE BIEN - GUARDAR SESION
                    sessionManager.saveUserSession(
                        userId = usuario.id,
                        userName = usuario.nombre,
                        apellidoP = usuario.apellidoP,
                        apellidoM = usuario.apellidoM,
                        userType = usuario.tipoUsuario,
                        numeroCasa = usuario.numeroCasa,
                        calle = usuario.calle,
                        firebaseId = usuario.firebaseID
                    )

                    val apellidos = "${usuario.apellidoP} ${usuario.apellidoM}".trim()
                    //println(" Login exitoso: $apellidos")
                    Toast.makeText(this@Login, "Bienvenido $apellidos", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@Login, Home::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                   // println("Error: Credenciales invAlidas en la API")
                    Toast.makeText(
                        this@Login,
                        "Error: Credenciales incorrectas o problema con el servidor",
                        Toast.LENGTH_LONG
                    ).show()

                    // Mensaje especifico
                    showAuthError()
                }

                // Volver a activar el boton
                btnLogin.isEnabled = true
            }
        }
    }

    //Mensaje de error
    private fun showAuthError() {
        Toast.makeText(
            this,
            "Verifica tu usuario y contraseña. Si el problema persiste, contacta al administrador.",
            Toast.LENGTH_LONG
        ).show()
    }

    // Para el modal
    private fun setupSoporteTecnico() {
        val tvSoporteTecnico = findViewById<TextView>(R.id.tvSoporteTecnico)

        tvSoporteTecnico.setOnClickListener {
            showSoporteTecnicoModal()
        }
    }

    //modal de soporte tecnico
    private fun showSoporteTecnicoModal() {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.modal_incoveniente)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            // bboton Continuar
            val btnContinuar = findViewById<CardView>(R.id.btnContinuar)
            btnContinuar.setOnClickListener {
                dismiss()
            }

            setCanceledOnTouchOutside(true)
        }

        dialog.show()

        // ajustar el tamaño
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 1),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}