package org.utl.reddeseguridadvecinal

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.utl.reddeseguridadvecinal.controller.ReportesController
import org.utl.reddeseguridadvecinal.modelo.ReporteRequest
import org.utl.reddeseguridadvecinal.modelo.TipoReporteResponse
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.io.ByteArrayOutputStream

class Realizar_reporte : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var selectableItemBackground: Drawable? = null

    private lateinit var switchAnonimo: SwitchCompat
    private lateinit var etNombre: EditText
    private lateinit var etUbicacion: EditText
    private lateinit var spinnerTipoReporte: Spinner
    private lateinit var etDescripcion: EditText
    private lateinit var btnGuardar: CardView
    private lateinit var btnAdjuntar: CardView
    private lateinit var tvEvidenciaPlaceholder: TextView

    private lateinit var sessionManager: SessionManager
    private val reportesController = ReportesController()
    private var userId: Int = -1
    private var nombreUsuario: String = ""
    private var direccionUsuario: String = ""
    private var listaTiposReporte: List<TipoReporteResponse> = emptyList()

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var imagenBase64: String? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private var latitudActual: Double = 21.1523
    private var longitudActual: Double = -101.711

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
        setContentView(R.layout.activity_realizar_reporte)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()
        nombreUsuario = "${sessionManager.getUserName()} ${sessionManager.getApellidosCompletos()}".trim()
        direccionUsuario = sessionManager.getDireccionCompleta()

        initViews()
        val rootLayout = findViewById<View>(R.id.main)
        setupHideKeyboardOnTouch(rootLayout)

        setupFormLogic()
        setupGalleryLauncher()
        setupLocationPermissionLauncher()
        cargarTiposDeReporte()
        obtenerUbicacionActual()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<CardView>(R.id.cvHeader)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
        setupDrawerMenuButton()
        setupDrawerItemListeners()
        setupDrawerHeader()
        highlightActiveMenuItem(R.id.llReportesMenu)

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

    private fun setupFormLogic() {
        etNombre.setText(nombreUsuario)
        etUbicacion.setText(direccionUsuario)

        switchAnonimo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etNombre.setText("Reporte Anónimo")
            } else {
                etNombre.setText(nombreUsuario)
            }
            etNombre.isEnabled = false
        }

        btnGuardar.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        btnAdjuntar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            galleryLauncher.launch(intent)
        }
    }

    private fun mostrarDialogoConfirmacion() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.modal_confirmar)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnConfirmarDialog = dialog.findViewById<CardView>(R.id.btnConfirmar)
        val btnCancelarDialog = dialog.findViewById<CardView>(R.id.btnCancelar)

        btnConfirmarDialog.setOnClickListener {
            dialog.dismiss()
            enviarReporte()
        }

        btnCancelarDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) obtenerUbicacionActual()
        }
    }
    private fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitudActual = location.latitude
                longitudActual = location.longitude
            }
        }
    }
    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)

        // Inicializar fondo seleccionable
        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        switchAnonimo = findViewById(R.id.switchAnonimo)
        etNombre = findViewById(R.id.etNombre)
        etUbicacion = findViewById(R.id.etUbicacion)
        spinnerTipoReporte = findViewById(R.id.spinnerTipoReporte)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnAdjuntar = findViewById(R.id.btnAdjuntar)
        tvEvidenciaPlaceholder = findViewById(R.id.tvEvidenciaPlaceholder)

        // Configurar boton cancelar
        findViewById<CardView>(R.id.btnCancelar).setOnClickListener {
            hideKeyboard()
            finish()
        }
    }
    private fun setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    imagenBase64 = null
                    tvEvidenciaPlaceholder.text = "Comprimiendo..."
                    btnGuardar.isEnabled = false
                    convertUriToBase64(uri)
                }
            }
        }
    }
    private fun convertUriToBase64(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            var base64String: String? = null
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    base64String = "data:image/jpeg;base64,$encoded"
                }
            } catch (e: Exception) { e.printStackTrace() }
            withContext(Dispatchers.Main) {
                if (base64String != null) {
                    imagenBase64 = base64String
                    tvEvidenciaPlaceholder.text = "Imagen adjuntada"
                } else {
                    tvEvidenciaPlaceholder.text = "Error"
                }
                btnGuardar.isEnabled = true
            }
        }
    }
    private fun cargarTiposDeReporte() {
        lifecycleScope.launch {
            val responseList = reportesController.getTiposDeReporte()
            if (responseList != null) {
                listaTiposReporte = responseList
                val nombres = responseList.map { it.nombre }
                val adapter = ArrayAdapter(this@Realizar_reporte, android.R.layout.simple_spinner_item, nombres)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTipoReporte.adapter = adapter
            }
        }
    }
    private fun enviarReporte() {
        val descripcion = etDescripcion.text.toString()
        val selectedPosition = spinnerTipoReporte.selectedItemPosition
        if (descripcion.isEmpty() || listaTiposReporte.isEmpty()) return

        val reporteRequest = ReporteRequest(
            usuarioID = if (switchAnonimo.isChecked) null else userId,
            tipoReporteID = listaTiposReporte[selectedPosition].tipoReporteID,
            titulo = listaTiposReporte[selectedPosition].nombre,
            descripcion = descripcion,
            latitud = latitudActual,
            longitud = longitudActual,
            direccionTexto = etUbicacion.text.toString(),
            esAnonimo = switchAnonimo.isChecked,
            imagenBase64 = imagenBase64 // Usa la variable Base64
        )

        lifecycleScope.launch {
            if (reportesController.crearReporte(reporteRequest)) {
                Toast.makeText(this@Realizar_reporte, "Enviado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@Realizar_reporte, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
    private fun setupHideKeyboardOnTouch(rootView: View) {
        if (rootView !is EditText) {
            rootView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) hideKeyboard()
                false
            }
        }
        if (rootView is ViewGroup) {
            for (child in rootView.children) setupHideKeyboardOnTouch(child)
        }
    }
    private fun setupDrawerMenuButton() { findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) } }
    private fun setupDrawerHeader() {
        val tvDrawerName = findViewById<TextView>(R.id.tvDrawerUserName)
        val tvDrawerAddress = findViewById<TextView>(R.id.tvDrawerUserAddress)
        if (sessionManager.getApellidosCompletos().isNotEmpty()) tvDrawerName.text = sessionManager.getApellidosCompletos() else tvDrawerName.text = "Usuario"
        tvDrawerAddress.text = sessionManager.getDireccionCompleta()
    }
    private fun setupStatusBar() {
        window.statusBarColor = Color.parseColor("#F5F5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
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
            if (targetActivity != Realizar_reporte::class.java) {
                startActivity(Intent(this, targetActivity))
                finish()
            }
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
}