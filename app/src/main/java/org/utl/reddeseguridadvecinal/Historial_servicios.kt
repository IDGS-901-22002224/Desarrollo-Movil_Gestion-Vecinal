package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.CargosServiciosController
import org.utl.reddeseguridadvecinal.controller.HistorialServiciosController
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Historial_servicios : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: SessionManager
    private lateinit var historialServiciosController: HistorialServiciosController
    private lateinit var cargosServiciosController: CargosServiciosController
    private var selectableItemBackground: Drawable? = null

    private val COLOR_ACTIVE_BG = Color.parseColor("#F0FDF4")
    private val COLOR_INACTIVE_BG = Color.WHITE
    private val COLOR_ACTIVE_TEXT = Color.parseColor("#047857")
    private val COLOR_INACTIVE_TEXT = Color.parseColor("#111827")

    private val COLOR_PENDIENTE = Color.parseColor("#FBBF24")
    private val COLOR_PROCESO = Color.parseColor("#4488EF")
    private val COLOR_CANCELADO = Color.parseColor("#EF4444")
    private val COLOR_COMPLETADO = Color.parseColor("#10B981")

    private val menuItemsToHighlight = listOf(
        R.id.llInicio, R.id.llReportesMenu, R.id.llAccesosMenu, R.id.llChatMenu,
        R.id.llMapaMenu, R.id.llServiciosMenu, R.id.llAvisosMenu, R.id.llPerfilMenu
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupStatusBar()
        setContentView(R.layout.activity_historial_servicios)

        sessionManager = SessionManager(this)
        historialServiciosController = HistorialServiciosController()
        cargosServiciosController = CargosServiciosController()

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

        cargarServicios()

        val btnSolicitarServicio = findViewById<CardView>(R.id.btnSolicitarServicio)
        btnSolicitarServicio.setOnClickListener {
            val intent = Intent(this, Solicitar_servicio::class.java)
            startActivity(intent)
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

    private fun cargarServicios() {
        val usuarioId = sessionManager.getUserId()
        if (usuarioId == -1) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val solicitudes = historialServiciosController.obtenerSolicitudesPorUsuario(usuarioId)
                mostrarServicios(solicitudes)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@Historial_servicios, "Error al cargar servicios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarServicios(solicitudes: List<org.utl.reddeseguridadvecinal.modelo.SolicitudServicio>) {
        val container = findViewById<LinearLayout>(R.id.llServiciosContainer)
        container.removeAllViews()

        // Filtrar servicios del mes actual
        val serviciosDelMes = filtrarServiciosDelMesActual(solicitudes)

        if (serviciosDelMes.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No hay servicios registrados en el mes actual"
                setTextColor(Color.parseColor("#666666"))
                textSize = 16f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                setPadding(0, 100, 0, 0)
                gravity = android.view.Gravity.CENTER
            }
            container.addView(emptyView)
            return
        }

        val inflater = LayoutInflater.from(this)

        val solicitudesOrdenadas = serviciosDelMes.sortedByDescending { it.fechaCreacion }

        solicitudesOrdenadas.forEach { solicitud ->
            val itemView = inflater.inflate(R.layout.item_servicio, container, false)
            configurarItemServicio(itemView, solicitud)
            container.addView(itemView)
        }
    }

    private fun filtrarServiciosDelMesActual(solicitudes: List<org.utl.reddeseguridadvecinal.modelo.SolicitudServicio>): List<org.utl.reddeseguridadvecinal.modelo.SolicitudServicio> {
        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH)
        val añoActual = calendario.get(Calendar.YEAR)

        return solicitudes.filter { solicitud ->
            try {
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val fechaSolicitud = formatoEntrada.parse(solicitud.fechaCreacion)

                if (fechaSolicitud != null) {
                    val calSolicitud = Calendar.getInstance()
                    calSolicitud.time = fechaSolicitud

                    val mesSolicitud = calSolicitud.get(Calendar.MONTH)
                    val añoSolicitud = calSolicitud.get(Calendar.YEAR)

                    mesSolicitud == mesActual && añoSolicitud == añoActual
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun configurarItemServicio(itemView: android.view.View, solicitud: org.utl.reddeseguridadvecinal.modelo.SolicitudServicio) {
        val viewBordeLateral = itemView.findViewById<android.view.View>(R.id.viewBordeLateral)
        val ivIconoEstado = itemView.findViewById<ImageView>(R.id.ivIconoEstado)
        val tvTipoServicio = itemView.findViewById<TextView>(R.id.tvTipoServicio)
        val tvFecha = itemView.findViewById<TextView>(R.id.tvFecha)
        val tvHorario = itemView.findViewById<TextView>(R.id.tvHorario)
        val tvCosto = itemView.findViewById<TextView>(R.id.tvCosto)

        // Configurar segun el estado
        when (solicitud.estado.toLowerCase(Locale.ROOT)) {
            "pendiente" -> {
                viewBordeLateral.setBackgroundColor(COLOR_PENDIENTE)
                ivIconoEstado.setImageResource(R.drawable.ic_clock)
                ivIconoEstado.setColorFilter(COLOR_PENDIENTE)
                tvCosto.visibility = android.view.View.GONE
            }
            "asignado", "en proceso" -> {
                viewBordeLateral.setBackgroundColor(COLOR_PROCESO)
                ivIconoEstado.setImageResource(R.drawable.ic_servicios)
                ivIconoEstado.setColorFilter(COLOR_PROCESO)
                tvCosto.visibility = android.view.View.GONE
            }
            "cancelado" -> {
                viewBordeLateral.setBackgroundColor(COLOR_CANCELADO)
                ivIconoEstado.setImageResource(R.drawable.ic_close_circle)
                ivIconoEstado.setColorFilter(COLOR_CANCELADO)
                tvCosto.visibility = android.view.View.GONE
            }
            "completado", "terminado" -> {
                viewBordeLateral.setBackgroundColor(COLOR_COMPLETADO)
                ivIconoEstado.setImageResource(R.drawable.ic_check)
                ivIconoEstado.setColorFilter(COLOR_COMPLETADO)
                tvCosto.visibility = android.view.View.VISIBLE

                // OBTENER COSTO
                obtenerCostoServicioReal(solicitud.solicitudID) { costo ->
                    if (costo > 0) {
                        tvCosto.text = "Costo $${String.format(Locale.US, "%.2f", costo)}"
                    } else {
                        tvCosto.text = "Costo por determinar"
                    }
                }
            }
            else -> {
                viewBordeLateral.setBackgroundColor(COLOR_PENDIENTE)
                ivIconoEstado.setImageResource(R.drawable.ic_clock)
                ivIconoEstado.setColorFilter(COLOR_PENDIENTE)
                tvCosto.visibility = android.view.View.GONE
            }
        }

        tvTipoServicio.text = solicitud.tipoServicioNombre ?: "Servicio"

        val fechaFormateada = formatearFecha(solicitud.fechaCreacion)
        tvFecha.text = fechaFormateada

        val horario = when {
            solicitud.horaPreferida != null ->
                "Horario: ${solicitud.horaPreferida}"
            else -> "Horario no especificado"
        }
        tvHorario.text = horario

        itemView.setOnClickListener {
            Toast.makeText(this, "${solicitud.tipoServicioNombre} - ${solicitud.estado}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerCostoServicioReal(solicitudId: Int, onCostoObtenido: (Double) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val cargos = cargosServiciosController.obtenerCargosPorSolicitud(solicitudId)
                val costoTotal = calcularCostoTotal(cargos)
                onCostoObtenido(costoTotal)
            } catch (e: Exception) {
                e.printStackTrace()
                onCostoObtenido(0.0)
            }
        }
    }

    private fun calcularCostoTotal(cargos: List<org.utl.reddeseguridadvecinal.modelo.CargoServicio>): Double {
        return cargos.sumOf { it.monto }
    }

    private fun formatearFecha(fechaString: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fecha = formatoEntrada.parse(fechaString)
            formatoSalida.format(fecha)
        } catch (e: Exception) {
            "Fecha no disponible"
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
        val sessionManager = SessionManager(this)

        sessionManager.clearSession()

        FirebaseAuth.getInstance().signOut()

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}