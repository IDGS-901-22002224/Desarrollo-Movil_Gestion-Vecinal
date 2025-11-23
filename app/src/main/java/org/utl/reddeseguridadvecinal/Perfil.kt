package org.utl.reddeseguridadvecinal

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.PerfilController
import org.utl.reddeseguridadvecinal.controller.PerfilLogica
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment
import org.utl.reddeseguridadvecinal.util.SessionManager
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

class Perfil : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var selectableItemBackground: Drawable? = null

    //campos del formulario
    private lateinit var etNombre: EditText
    private lateinit var etApellidoPaterno: EditText
    private lateinit var etApellidoMaterno: EditText
    private lateinit var etNumeroCasa: EditText
    private lateinit var etCalle: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etFechaNacimiento: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etNumeroTarjeta: EditText
    private lateinit var etFechaVencimiento: EditText

    // Botones
    private lateinit var btnGuardar: CardView
    private lateinit var btnCancelar: CardView

    private lateinit var sessionManager: SessionManager
    private lateinit var perfilController: PerfilController
    private lateinit var perfilLogica: PerfilLogica

    // Colores del menu lateral
    private val COLOR_ACTIVE_BG = Color.parseColor("#F0FDF4")
    private val COLOR_INACTIVE_BG = Color.WHITE
    private val COLOR_ACTIVE_TEXT = Color.parseColor("#047857")
    private val COLOR_INACTIVE_TEXT = Color.parseColor("#111827")

    private val menuItemsToHighlight = listOf(
        R.id.llInicio, R.id.llReportesMenu, R.id.llAccesosMenu, R.id.llChatMenu,
        R.id.llMapaMenu, R.id.llServiciosMenu, R.id.llAvisosMenu, R.id.llPerfilMenu
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupStatusBar()
        setContentView(R.layout.activity_perfil)

        sessionManager = SessionManager(this)
        perfilController = PerfilController(sessionManager)
        perfilLogica = PerfilLogica(this, sessionManager, perfilController)

        initViews()
        setupClickListeners()
        cargarDatosUsuario()

        drawerLayout = findViewById(R.id.drawer_layout)
        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        val cvHeader = findViewById<CardView>(R.id.cvHeader)
        ViewCompat.setOnApplyWindowInsetsListener(cvHeader) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        setupDrawerMenuButton()
        setupDrawerItemListeners()

        highlightActiveMenuItem(R.id.llPerfilMenu)

        // boton de retroceso
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun initViews() {
        // EditText
        etNombre = findViewById(R.id.etNombre)
        etApellidoPaterno = findViewById(R.id.etApellidoPaterno)
        etApellidoMaterno = findViewById(R.id.etApellidoMaterno)
        etNumeroCasa = findViewById(R.id.etNumeroCasa)
        etCalle = findViewById(R.id.etCalle)
        etTelefono = findViewById(R.id.etTelefono)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta)
        etFechaVencimiento = findViewById(R.id.etFechaVencimiento)

        // Botones
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun setupClickListeners() {
        btnGuardar.setOnClickListener {
            mostrarConfirmacionGuardar()
        }

        btnCancelar.setOnClickListener {
            cargarDatosUsuario()
            Toast.makeText(this, "Cambios cancelados", Toast.LENGTH_SHORT).show()
        }

        // DatePicker para fecha de nacimiento
        val cvFechaNacimiento = findViewById<CardView>(R.id.cvFechaNacimiento)
        cvFechaNacimiento.setOnClickListener {
            perfilLogica.mostrarDatePickerPerfil(this, etFechaNacimiento)
        }

        etFechaNacimiento.setOnClickListener {
            perfilLogica.mostrarDatePickerPerfil(this, etFechaNacimiento)
        }

        perfilLogica.configurarTextWatcherTelefono(etTelefono)
        perfilLogica.configurarTextWatcherFechaVencimiento(etFechaVencimiento)
        perfilLogica.configurarTextWatcherNumeroTarjeta(etNumeroTarjeta)
    }

    private fun mostrarConfirmacionGuardar() {
        val dialogFragment = ConfirmDialogFragment.newInstance(
            titulo = "GUARDAR CAMBIOS",
            mensajePrincipal = "¿Estás seguro de que quieres guardar los cambios?",
            mensajeSecundario = "Los datos de tu perfil se actualizarán con la nueva información",
            textoBotonConfirmar = "Actualizar",
            textoBotonCancelar = "Cancelar",
            onConfirm = {
                guardarCambios()
            }
        )
        dialogFragment.show(supportFragmentManager, "GuardarConfirmDialog")
    }

    private fun cargarDatosUsuario() {
        // campos no editables
        etNombre.setText(sessionManager.getUserName())
        etApellidoPaterno.setText(sessionManager.getApellidoP())
        etApellidoMaterno.setText(sessionManager.getApellidoM())
        etNumeroCasa.setText(sessionManager.getNumeroCasa())
        etCalle.setText(sessionManager.getCalle())

        // datos desde la API
        CoroutineScope(Dispatchers.Main).launch {
            val usuarioId = sessionManager.getUserId()
            if (usuarioId != -1) {
                try {
                    val usuario = perfilController.getUsuarioById(usuarioId)
                    usuario?.let {
                        etTelefono.setText(perfilLogica.formatTelefono(it.telefono ?: ""))
                        etEmail.setText(it.email ?: "")

                        // FECHA DE NACIMIENTO-  formato yyyy-MM-dd
                        etFechaNacimiento.setText(it.fechaNacimiento ?: "")

                        // MASCARA DE TARJETA
                        val ultimosDigitos = it.ultimosDigitos ?: ""
                        if (ultimosDigitos.isNotEmpty() && ultimosDigitos.length == 4) {
                            val tarjetaConMascara = "****-****-****-$ultimosDigitos"
                            etNumeroTarjeta.hint = tarjetaConMascara
                            etNumeroTarjeta.setText("")
                        } else {
                            etNumeroTarjeta.hint = "****-****-****-****"
                        }

                        // MASCARA FECHA VENCIMIENTO
                        etFechaVencimiento.hint = "MM-YY"
                        etFechaVencimiento.setText(perfilLogica.formatFechaVencimiento(it.fechaVencimiento ?: ""))

                        etPassword.setText("")
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Perfil, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@Perfil, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarCambios() {
        val updateRequest = perfilLogica.prepararDatosActualizacion(
            etNombre.text.toString(),
            etApellidoPaterno.text.toString(),
            etApellidoMaterno.text.toString(),
            etNumeroCasa.text.toString(),
            etCalle.text.toString(),
            etTelefono.text.toString(),
            etFechaNacimiento.text.toString(),
            etEmail.text.toString(),
            etPassword.text.toString(),
            etNumeroTarjeta.text.toString(),
            etFechaVencimiento.text.toString()
        )

        if (updateRequest == null) {
            // SI SALE LA VALIDACION
            return
        }

        btnGuardar.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = perfilController.updateUsuario(updateRequest)
                if (success) {
                    Toast.makeText(this@Perfil, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                    // Recargar datos actualizados
                    cargarDatosUsuario()
                } else {
                    Toast.makeText(this@Perfil, "Error al actualizar datos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Perfil, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                btnGuardar.isEnabled = true
            }
        }
    }

    private fun setupDrawerMenuButton() {
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupStatusBar() {
        window.statusBarColor = Color.parseColor("#F5F5F5")
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }
    }

     // elemento de menu activo
    private fun highlightActiveMenuItem(activeLayoutId: Int) {
        val navDrawerContent = findViewById<LinearLayout>(R.id.nav_drawer_content)
        val menuContainer = navDrawerContent.getChildAt(1) as LinearLayout

        for (i in 0 until menuContainer.childCount) {
            val child = menuContainer.getChildAt(i)

            if (child is LinearLayout && menuItemsToHighlight.contains(child.id)) {

                val isActive = child.id == activeLayoutId

                child.setBackgroundColor(if (isActive) COLOR_ACTIVE_BG else COLOR_INACTIVE_BG)
                child.foreground = if (!isActive) selectableItemBackground else null

                if (child.childCount >= 2) {
                    val icon = child.getChildAt(0) as ImageView
                    val text = child.getChildAt(1) as TextView

                    val textColor = if (isActive) COLOR_ACTIVE_TEXT else COLOR_INACTIVE_TEXT

                    icon.setColorFilter(textColor)
                    text.setTextColor(textColor)

                    text.setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)
                }
            }
        }
    }

    private fun setupDrawerItemListeners() {
        val llInicio = findViewById<LinearLayout>(R.id.llInicio)
        val llReportes = findViewById<LinearLayout>(R.id.llReportesMenu)
        val llAccesos = findViewById<LinearLayout>(R.id.llAccesosMenu)
        val llChat = findViewById<LinearLayout>(R.id.llChatMenu)
        val llMapa = findViewById<LinearLayout>(R.id.llMapaMenu)
        val llServicios = findViewById<LinearLayout>(R.id.llServiciosMenu)
        val llAvisos = findViewById<LinearLayout>(R.id.llAvisosMenu)
        val llPerfil = findViewById<LinearLayout>(R.id.llPerfilMenu)
        val llCerrarSesion = findViewById<LinearLayout>(R.id.llCerrarSesion)

        // Actualizar encabezado
        updateDrawerHeader()

        fun navigateAndFinish(targetActivity: Class<*>) {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, targetActivity))
            finish()
        }

        llInicio.setOnClickListener { navigateAndFinish(Home::class.java) }
        llReportes.setOnClickListener { navigateAndFinish(Reportes::class.java) }
        llAccesos.setOnClickListener { navigateAndFinish(Acceso::class.java) }
        llChat.setOnClickListener { navigateAndFinish(Chat_vecinal::class.java) }
        llMapa.setOnClickListener { navigateAndFinish(Mapa::class.java) }
        llServicios.setOnClickListener { navigateAndFinish(Pagos_Servicios::class.java) }
        llAvisos.setOnClickListener { navigateAndFinish(Avisos_vecinales::class.java) }

        llPerfil.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }

        llCerrarSesion.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            showLogoutConfirmation()
        }
    }

    // Metodo para el encabezado del menu
    private fun updateDrawerHeader() {
        val apellidos = sessionManager.getApellidosCompletos()
        val direccion = sessionManager.getDireccionCompleta()

        val navDrawerContent = findViewById<LinearLayout>(R.id.nav_drawer_content)
        val headerLayout = navDrawerContent.getChildAt(0) as LinearLayout

        val tvNombreUsuario = headerLayout.getChildAt(0) as? TextView
        val tvCasa = headerLayout.getChildAt(1) as? TextView

        tvNombreUsuario?.text = apellidos
        tvCasa?.text = direccion
    }

    // cerrar sesion
    private fun showLogoutConfirmation() {
        val dialogFragment = ConfirmDialogFragment.newInstance(
            titulo = "CERRAR SESIÓN",
            mensajePrincipal = "¿Estás seguro de que quieres cerrar sesión?",
            mensajeSecundario = "Tendrás que volver a iniciar sesión para reingresar",
            textoBotonConfirmar = "Cerrar sesión",
            textoBotonCancelar = "Cancelar",
            onConfirm = {
                performLogout()
            }
        )
        dialogFragment.show(supportFragmentManager, "LogoutConfirmDialog")
    }

    private fun performLogout() {
        perfilLogica.cerrarSesion()

        // Redirigir al login
        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    // --- OCULTAR TECLADO ---
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}