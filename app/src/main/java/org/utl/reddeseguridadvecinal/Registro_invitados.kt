package org.utl.reddeseguridadvecinal

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.AccesosController
import org.utl.reddeseguridadvecinal.modelo.InvitadoRequest
import org.utl.reddeseguridadvecinal.util.QRGenerator
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.util.Calendar

class Registro_invitados : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // Vistas del Formulario
    private lateinit var etNombre: EditText
    private lateinit var etApellidoPaterno: EditText
    private lateinit var etApellidoMaterno: EditText
    private lateinit var etDomicilio: EditText
    private lateinit var etFecha: EditText
    private lateinit var btnGuardar: CardView

    // Lógica y Datos
    private lateinit var sessionManager: SessionManager
    private val accesosController = AccesosController()
    private var userId: Int = -1
    private var fechaSeleccionadaAPI: String = ""

    // Colores del menú
    private val COLOR_ACTIVE_BG = Color.parseColor("#F0FDF4")
    private val COLOR_INACTIVE_BG = Color.WHITE
    private val COLOR_ACTIVE_TEXT = Color.parseColor("#047857")
    private val COLOR_INACTIVE_TEXT = Color.parseColor("#111827")
    private val menuItemsToHighlight = listOf(
        R.id.llInicio, R.id.llReportesMenu, R.id.llAccesosMenu, R.id.llChatMenu,
        R.id.llMapaMenu, R.id.llServiciosMenu, R.id.llAvisosMenu, R.id.llPerfilMenu
    )
    private var selectableItemBackground: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupStatusBar()
        setContentView(R.layout.activity_registro_invitados)

        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()

        initViews()
        setupFormLogic()

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
        setupDrawerHeader()
        highlightActiveMenuItem(R.id.llAccesosMenu)

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
        etNombre = findViewById(R.id.etNombre)
        etApellidoPaterno = findViewById(R.id.etApellidoPaterno)
        etApellidoMaterno = findViewById(R.id.etApellidoMaterno)
        etDomicilio = findViewById(R.id.etDomicilio)
        etFecha = findViewById(R.id.etFecha)
        btnGuardar = findViewById(R.id.btnGuardar)

        val btnCancelar = findViewById<CardView>(R.id.btnCancelar)
        btnCancelar.setOnClickListener { finish() }
    }

    private fun setupFormLogic() {
        etDomicilio.setText(sessionManager.getDireccionCompleta())
        etDomicilio.isEnabled = false

        etFecha.setOnClickListener {
            mostrarDatePicker()
        }

        btnGuardar.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    private fun mostrarDialogoConfirmacion() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.modal_confirmar)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvTitulo = dialog.findViewById<TextView>(R.id.tvTituloAlerta)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvMensajePrincipal)
        val tvSubmensaje = dialog.findViewById<TextView>(R.id.tvMensajeSecundario)

        tvTitulo.text = "REGISTRAR INVITADO"
        tvMensaje.text = "¿Los datos del invitado son correctos?"
        tvSubmensaje.text = "Se generará un código QR de acceso."

        val btnConfirmarDialog = dialog.findViewById<CardView>(R.id.btnConfirmar)
        val btnCancelarDialog = dialog.findViewById<CardView>(R.id.btnCancelar)

        btnConfirmarDialog.setOnClickListener {
            dialog.dismiss()
            guardarInvitado()
        }

        btnCancelarDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val mesReal = month + 1
            val fechaMostrar = String.format("%02d/%02d/%04d", dayOfMonth, mesReal, year)
            etFecha.setText(fechaMostrar)
            fechaSeleccionadaAPI = String.format("%04d-%02d-%02dT23:59:59", year, mesReal, dayOfMonth)
        }, anio, mes, dia)

        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun guardarInvitado() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }

        val nombre = etNombre.text.toString().trim()
        val paterno = etApellidoPaterno.text.toString().trim()
        val materno = etApellidoMaterno.text.toString().trim()

        if (nombre.isEmpty() || paterno.isEmpty() || fechaSeleccionadaAPI.isEmpty()) {
            Toast.makeText(this, "Por favor completa nombre, apellido paterno y fecha", Toast.LENGTH_SHORT).show()
            return
        }

        val request = InvitadoRequest(
            usuarioID = userId,
            nombreInvitado = nombre,
            apellidoPaternoInvitado = paterno,
            apellidoMaternoInvitado = materno,
            fechaVencimiento = fechaSeleccionadaAPI
        )

        btnGuardar.isEnabled = false

        lifecycleScope.launch {
            val exitoCreacion = accesosController.crearInvitado(request)

            if (exitoCreacion) {
                Toast.makeText(this@Registro_invitados, "Generando código QR...", Toast.LENGTH_SHORT).show()

                val historial = accesosController.getHistorialInvitados(userId)

                val ultimoInvitado = historial?.maxByOrNull { it.fechaGeneracion }

                if (ultimoInvitado != null && ultimoInvitado.codigoQR.isNotEmpty()) {
                    val qrBitmap = QRGenerator.generateQRCode(ultimoInvitado.codigoQR, 512, 512)

                    if (qrBitmap != null) {
                        val nombreArchivo = "QR_${nombre}_${paterno}"
                        QRGenerator.guardarImagenEnGaleria(this@Registro_invitados, qrBitmap, nombreArchivo)
                    }
                }

                // 5. Finalizamos
                Toast.makeText(this@Registro_invitados, "Invitación creada con éxito", Toast.LENGTH_SHORT).show()
                finish()

            } else {
                Toast.makeText(this@Registro_invitados, "Error al crear invitación", Toast.LENGTH_LONG).show()
                btnGuardar.isEnabled = true
            }
        }
    }

    private fun setupDrawerHeader() {
        val tvDrawerName = findViewById<TextView>(R.id.tvDrawerUserName)
        val tvDrawerAddress = findViewById<TextView>(R.id.tvDrawerUserAddress)
        val apellidos = sessionManager.getApellidosCompletos()
        val direccion = sessionManager.getDireccionCompleta()
        if (apellidos.isNotEmpty()) {
            tvDrawerName.text = apellidos
        } else {
            tvDrawerName.text = "Usuario"
        }
        tvDrawerAddress.text = direccion
    }

    private fun setupDrawerMenuButton() {
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupStatusBar() {
        window.statusBarColor = Color.parseColor("#F5F5F5")
        val wic = WindowCompat.getInsetsController(window, window.decorView)
        wic.isAppearanceLightStatusBars = true
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

        fun navigateAndHighlight(targetActivity: Class<*>, activeLayoutId: Int) {
            highlightActiveMenuItem(activeLayoutId)
            drawerLayout.closeDrawer(GravityCompat.START)
            if (targetActivity != Registro_invitados::class.java) {
                startActivity(Intent(this, targetActivity))
                finish()
            }
        }

        fun navigateAndFinish(target: Class<*>) {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, target))
            finish()
        }

        llInicio.setOnClickListener { navigateAndHighlight(Home::class.java, R.id.llInicio) }
        llReportes.setOnClickListener { navigateAndHighlight(Reportes::class.java, R.id.llReportesMenu) }
        llAccesos.setOnClickListener { navigateAndHighlight(Acceso::class.java, R.id.llAccesosMenu) }
        llChat.setOnClickListener { navigateAndHighlight(Chat_vecinal::class.java, R.id.llChatMenu) }
        llMapa.setOnClickListener { navigateAndHighlight(Mapa::class.java, R.id.llMapaMenu) }
        llServicios.setOnClickListener { navigateAndHighlight(Pagos_Servicios::class.java, R.id.llServiciosMenu) }
        llAvisos.setOnClickListener { navigateAndHighlight(Avisos_vecinales::class.java, R.id.llAvisosMenu) }
        llPerfil.setOnClickListener { navigateAndHighlight(Perfil::class.java, R.id.llPerfilMenu) }

        llCerrarSesion.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            sessionManager.clearSession()
            val intent = Intent(this, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}