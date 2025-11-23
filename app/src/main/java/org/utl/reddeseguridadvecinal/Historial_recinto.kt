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
import org.utl.reddeseguridadvecinal.controller.ReservasController
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Historial_recinto : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: SessionManager
    private lateinit var reservasController: ReservasController
    private var selectableItemBackground: Drawable? = null
    private lateinit var reservasContainer: LinearLayout

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
        setContentView(R.layout.activity_historial_recinto)

        sessionManager = SessionManager(this)
        reservasController = ReservasController()

        reservasContainer = findViewById(R.id.reservasContainer)

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

        cargarReservasUsuario()

        val btnReservar = findViewById<CardView>(R.id.btnReservar)
        btnReservar.setOnClickListener {
            val intent = Intent(this, Solicitar_recinto::class.java)
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

    private fun cargarReservasUsuario() {
        val usuarioId = sessionManager.getUserId()

        if (usuarioId == -1) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val reservas = reservasController.obtenerReservasPorUsuario(usuarioId)

                if (reservas.isNotEmpty()) {
                    mostrarReservas(reservas)
                } else {
                    mostrarMensajeSinReservas()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Historial_recinto, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarReservas(reservas: List<org.utl.reddeseguridadvecinal.modelo.ReservaDTO>) {

        reservasContainer.removeAllViews()

        val reservasFiltradas = filtrarReservasProximosMeses(reservas)

        if (reservasFiltradas.isEmpty()) {
            mostrarMensajeSinReservasProximas()
            return
        }

        val reservasOrdenadas = reservasFiltradas.sortedBy { it.fechaReserva }

        reservasOrdenadas.forEach { reserva ->
            val tarjetaReserva = crearTarjetaReserva(reserva)
            reservasContainer.addView(tarjetaReserva)
        }
    }

    private fun filtrarReservasProximosMeses(reservas: List<org.utl.reddeseguridadvecinal.modelo.ReservaDTO>): List<org.utl.reddeseguridadvecinal.modelo.ReservaDTO> {
        val calendario = Calendar.getInstance()
        val fechaActual = calendario.time

        calendario.add(Calendar.MONTH, 2)
        val fechaLimite = calendario.time

        return reservas.filter { reserva ->
            try {
                val fechaReservaString = reserva.fechaReserva
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaReserva = formatoEntrada.parse(fechaReservaString)

                if (fechaReserva != null) {
                    !fechaReserva.before(fechaActual) && !fechaReserva.after(fechaLimite)
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun crearTarjetaReserva(reserva: org.utl.reddeseguridadvecinal.modelo.ReservaDTO): CardView {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_reserva, reservasContainer, false) as CardView

        val ivIcono = cardView.findViewById<ImageView>(R.id.ivIconoEstado)
        val tvNombreEspacio = cardView.findViewById<TextView>(R.id.tvNombreEspacio)
        val tvFecha = cardView.findViewById<TextView>(R.id.tvFecha)
        val tvHorario = cardView.findViewById<TextView>(R.id.tvHorario)

        tvNombreEspacio.text = reserva.amenidadNombre ?: "Espacio no disponible"
        tvFecha.text = formatearFecha(reserva.fechaReserva)
        tvHorario.text = "Inicio: ${formatearHora(reserva.horaInicio)} - Fin: ${formatearHora(reserva.horaFin)}"

        when (reserva.estado?.lowercase() ?: "pendiente") {
            "pendiente" -> {
                ivIcono.setImageResource(R.drawable.ic_clock)
                ivIcono.setColorFilter(Color.parseColor("#F59E0B"))
            }
            "cancelada" -> {
                ivIcono.setImageResource(R.drawable.ic_close_circle)
                ivIcono.setColorFilter(Color.parseColor("#EF4444"))
            }
            "aceptada" -> {
                ivIcono.setImageResource(R.drawable.ic_check)
                ivIcono.setColorFilter(Color.parseColor("#10B981"))
            }
            else -> {
                ivIcono.setImageResource(R.drawable.ic_clock)
                ivIcono.setColorFilter(Color.parseColor("#6B7280"))
            }
        }

        return cardView
    }

    private fun obtenerInfoCreacion(fechaCreacion: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val fechaCreacionDate = formatoEntrada.parse(fechaCreacion)

            if (fechaCreacionDate != null) {
                val calendarioCreacion = Calendar.getInstance()
                calendarioCreacion.time = fechaCreacionDate

                val calendarioActual = Calendar.getInstance()
                val diferenciaMeses = (calendarioActual.get(Calendar.YEAR) * 12 + calendarioActual.get(Calendar.MONTH)) -
                        (calendarioCreacion.get(Calendar.YEAR) * 12 + calendarioCreacion.get(Calendar.MONTH))

                when {
                    diferenciaMeses == 0 -> "Creada este mes"
                    diferenciaMeses == 1 -> "Creada el mes pasado"
                    else -> "Creada hace $diferenciaMeses meses"
                }
            } else {
                "Fecha de creación no disponible"
            }
        } catch (e: Exception) {
            "Fecha de creación no disponible"
        }
    }

    private fun mostrarMensajeSinReservas() {
        val textView = TextView(this).apply {
            text = "No tienes reservas realizadas"
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 16f
            setTypeface(typeface, Typeface.ITALIC)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }
        reservasContainer.addView(textView)
    }

    private fun mostrarMensajeSinReservasProximas() {
        val textView = TextView(this).apply {
            text = "No tienes reservas para el mes actual ni los próximos 2 meses"
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 16f
            setTypeface(typeface, Typeface.ITALIC)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }
        reservasContainer.addView(textView)
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            if (fecha.contains("T")) {
                fecha.split("T")[0]
            } else {
                fecha
            }
        } catch (e: Exception) {
            fecha
        }
    }

    private fun formatearHora(hora: String): String {
        return try {
            if (hora.length > 5) {
                hora.substring(0, 5)
            } else {
                hora
            }
        } catch (e: Exception) {
            hora
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