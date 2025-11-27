package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment
import org.utl.reddeseguridadvecinal.logica.SolicitarRecintoLogica
import org.utl.reddeseguridadvecinal.util.SessionManager

class Solicitar_recinto : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: SessionManager
    private var selectableItemBackground: Drawable? = null
    private lateinit var solicitarRecintoLogica: SolicitarRecintoLogica

    private lateinit var spinnerRecinto: Spinner
    private lateinit var etDetalles: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHoraInicio: EditText
    private lateinit var etHoraFin: EditText
    private lateinit var btnGuardar: CardView
    private lateinit var btnCancelar: CardView

    private var detallesTemporales: String = ""
    private var fechaTemporal: String = ""
    private var horaInicioTemporal: String = ""
    private var horaFinTemporal: String = ""
    private var recintoTemporal: String = ""
    private var usuarioIdTemporal: Int = -1

    // Colores del menú
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
        setContentView(R.layout.activity_solicitar_recinto)

        sessionManager = SessionManager(this)
        solicitarRecintoLogica = SolicitarRecintoLogica()

        initViews()

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

        highlightActiveMenuItem(R.id.llServiciosMenu)

        updateDrawerHeader()

        setupListeners()

        cargarAmenidades()

        val rootView = findViewById<ConstraintLayout>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
        spinnerRecinto = findViewById(R.id.spinnerServicio)
        etDetalles = findViewById(R.id.etDetalles)
        etFecha = findViewById(R.id.etFecha)
        etHoraInicio = findViewById(R.id.etHoraInicio)
        etHoraFin = findViewById(R.id.etHoraFin)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun setupListeners() {
        // DatePicker para fecha
        etFecha.setOnClickListener {
            solicitarRecintoLogica.mostrarDatePicker(this, etFecha)
        }

        // TimePicker para hora inicio
        etHoraInicio.setOnClickListener {
            solicitarRecintoLogica.mostrarTimePicker(this, etHoraInicio)
        }

        // TimePicker para hora fin
        etHoraFin.setOnClickListener {
            solicitarRecintoLogica.mostrarTimePicker(this, etHoraFin)
        }

        // Guardar
        btnGuardar.setOnClickListener {
            validarYConfirmarReserva()
        }

        // Cancelar
        btnCancelar.setOnClickListener {
            navigateToHistorial()
        }
    }

    private fun cargarAmenidades() {
        solicitarRecintoLogica.cargarAmenidades(
            context = this,
            onSuccess = { amenidades ->
                llenarSpinnerRecintos(amenidades)
            },
            onError = { mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun llenarSpinnerRecintos(amenidades: List<org.utl.reddeseguridadvecinal.modelo.AmenidadDTO>) {
        val nombresAmenidades = amenidades.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombresAmenidades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecinto.adapter = adapter
    }

    private fun validarYConfirmarReserva() {
        usuarioIdTemporal = sessionManager.getUserId()
        detallesTemporales = etDetalles.text.toString().trim()
        fechaTemporal = etFecha.text.toString().trim()
        horaInicioTemporal = etHoraInicio.text.toString().trim()
        horaFinTemporal = etHoraFin.text.toString().trim()
        recintoTemporal = spinnerRecinto.selectedItem?.toString() ?: ""

        // Validar campos obligatorios
        val error = when {
            usuarioIdTemporal == -1 -> "Error: Usuario no identificado"
            recintoTemporal.isEmpty() -> "Seleccione un recinto"
            detallesTemporales.isEmpty() -> "Ingrese los detalles de la reserva"
            fechaTemporal.isEmpty() -> "Seleccione una fecha"
            horaInicioTemporal.isEmpty() -> "Seleccione la hora de inicio"
            horaFinTemporal.isEmpty() -> "Seleccione la hora de fin"
            else -> null
        }

        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            return
        }

        showReservaConfirmation()
    }

    private fun showReservaConfirmation() {
        val dialogFragment = ConfirmDialogFragment.newInstance(
            titulo = "CONFIRMAR RESERVA",
            mensajePrincipal = "¿Estás seguro de que quieres realizar esta reserva?",
            mensajeSecundario = "La solicitud sera enviada y no se podra alterar",
            textoBotonConfirmar = "Enviar solicitud",
            textoBotonCancelar = "Modificar",
            onConfirm = {
                enviarReservaConfirmada()
            }
        )
        dialogFragment.show(supportFragmentManager, "ReservaConfirmDialog")
    }

    private fun enviarReservaConfirmada() {
        solicitarRecintoLogica.validarYGuardarReserva(
            context = this,
            usuarioId = usuarioIdTemporal,
            spinnerRecinto = spinnerRecinto,
            detalles = detallesTemporales,
            fecha = fechaTemporal,
            horaInicio = horaInicioTemporal,
            horaFin = horaFinTemporal,
            onSuccess = {
                Toast.makeText(this, "Reserva creada exitosamente", Toast.LENGTH_SHORT).show()
                navigateToHistorial()
            },
            onError = { mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun navigateToHistorial() {
        val intent = Intent(this, Historial_recinto::class.java)
        startActivity(intent)
        finish()
    }

    // --- COMPARTIDOS DEL MENU ---

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
        llPerfil.setOnClickListener { navigateAndFinish(Perfil::class.java) }

        llCerrarSesion.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            showLogoutConfirmation()
        }
    }

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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }

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
        solicitarRecintoLogica.cerrarSesion(this)
        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}