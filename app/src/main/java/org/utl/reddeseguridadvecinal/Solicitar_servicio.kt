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
import android.widget.AdapterView
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
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment
import org.utl.reddeseguridadvecinal.logica.SolicitarServicioLogica
import org.utl.reddeseguridadvecinal.modelo.TipoServicioDTO
import org.utl.reddeseguridadvecinal.util.SessionManager

class Solicitar_servicio : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: SessionManager
    private var selectableItemBackground: Drawable? = null
    private lateinit var solicitarServicioLogica: SolicitarServicioLogica

    private lateinit var spinnerServicio: Spinner
    private lateinit var spinnerUrgencia: Spinner
    private lateinit var etDetalles: TextView
    private lateinit var etFecha: TextView
    private lateinit var etHora: TextView
    private lateinit var btnGuardar: CardView

    private var tiposServicio: List<TipoServicioDTO> = emptyList()
    private var servicioSeleccionado: TipoServicioDTO? = null
    private var urgenciaSeleccionada: String? = null

    //almacenar los datos temporalmente
    private var detallesTemporales: String = ""
    private var fechaTemporal: String = ""
    private var horaTemporal: String = ""
    private var usuarioIDTemporal: Int = -1

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
        setContentView(R.layout.activity_solicitar_servicio)

        sessionManager = SessionManager(this)
        solicitarServicioLogica = SolicitarServicioLogica()

        initViews()
        setupClickListeners()
        cargarServicios()
        configurarSpinnerUrgencia()

        setupDrawerComponents()
    }

    private fun initViews() {
        spinnerServicio = findViewById(R.id.spinnerServicio)
        spinnerUrgencia = findViewById(R.id.spinnerUrgencia)
        etDetalles = findViewById(R.id.etDetalles)
        etFecha = findViewById(R.id.etFecha)
        etHora = findViewById(R.id.etHora)
        btnGuardar = findViewById(R.id.btnGuardar)
    }

    private fun configurarSpinnerUrgencia() {
        val adapter = solicitarServicioLogica.crearAdaptadorUrgencia(this)
        spinnerUrgencia.adapter = adapter

        spinnerUrgencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                urgenciaSeleccionada = solicitarServicioLogica.nivelesUrgencia[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                urgenciaSeleccionada = null
            }
        }
    }

    private fun setupClickListeners() {
        // Date Picker
        etFecha.setOnClickListener {
            solicitarServicioLogica.mostrarDatePicker(this) { fecha ->
                etFecha.text = fecha
            }
        }

        // Time Picker
        etHora.setOnClickListener {
            solicitarServicioLogica.mostrarTimePicker(this) { hora ->
                etHora.text = hora
            }
        }

        // Guardar solicitud
        btnGuardar.setOnClickListener {
            validarYConfirmarSolicitud()
        }
    }

    private fun cargarServicios() {
        solicitarServicioLogica.cargarServiciosEnSpinner(
            context = this,
            onServiciosCargados = { servicios ->
                tiposServicio = servicios
                val adapter = solicitarServicioLogica.crearAdaptadorSpinner(this, servicios)
                spinnerServicio.adapter = adapter

                spinnerServicio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (position >= 0 && position < tiposServicio.size) {
                            servicioSeleccionado = tiposServicio[position]
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        servicioSeleccionado = null
                    }
                }
            },
            onError = { mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun validarYConfirmarSolicitud() {
        detallesTemporales = etDetalles.text.toString()
        fechaTemporal = etFecha.text.toString()
        horaTemporal = etHora.text.toString()
        usuarioIDTemporal = sessionManager.getUserId()

        val error = solicitarServicioLogica.validarFormulario(
            servicioSeleccionado,
            detallesTemporales,
            urgenciaSeleccionada,
            fechaTemporal,
            horaTemporal
        )

        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            return
        }

        if (usuarioIDTemporal == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        showSolicitudConfirmation()
    }

    private fun showSolicitudConfirmation() {
        val servicioNombre = servicioSeleccionado?.nombre ?: "Servicio no seleccionado"
        val urgencia = urgenciaSeleccionada ?: "No especificada"

        val dialogFragment = ConfirmDialogFragment.newInstance(
            titulo = "CONFIRMAR SOLICITUD",
            mensajePrincipal = "¿Estás seguro de que quieres enviar esta solicitud de servicio?",
            mensajeSecundario = "La solicitud sera enviada y no se podra alterar",
            textoBotonConfirmar = "Enviar solicitud",
            textoBotonCancelar = "Modificar",
            onConfirm = {
                enviarSolicitudConfirmada()
            }
        )
        dialogFragment.show(supportFragmentManager, "SolicitudConfirmDialog")
    }

    private fun enviarSolicitudConfirmada() {
        solicitarServicioLogica.enviarSolicitud(
            context = this,
            usuarioID = usuarioIDTemporal,
            servicioSeleccionado = servicioSeleccionado!!,
            detalles = detallesTemporales,
            urgencia = urgenciaSeleccionada!!,
            fecha = fechaTemporal,
            hora = horaTemporal,
            onSuccess = {
                Toast.makeText(this, "Solicitud creada exitosamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Historial_servicios::class.java)
                startActivity(intent)
                finish()
            },
            onError = { mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupDrawerComponents() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        setupDrawerMenuButton()
        setupDrawerItemListeners()
        highlightActiveMenuItem(R.id.llServiciosMenu)
        updateDrawerHeader()

        val cvHeader = findViewById<CardView>(R.id.cvHeader)
        ViewCompat.setOnApplyWindowInsetsListener(cvHeader) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // cancelar
        val btnCancelar = findViewById<CardView>(R.id.btnCancelar)
        btnCancelar.setOnClickListener {
            val intent = Intent(this, Historial_servicios::class.java)
            startActivity(intent)
            finish()
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
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

        fun navigateAndFinish(target: Class<*>) {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, target))
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
        solicitarServicioLogica.cerrarSesion(this)
        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}