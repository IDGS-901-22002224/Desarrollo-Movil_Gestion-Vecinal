package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.AvisosController
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment
import org.utl.reddeseguridadvecinal.modelo.Aviso
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

class Avisos_vecinales : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var llAvisosContainer: LinearLayout
    private lateinit var avisosController: AvisosController
    private lateinit var sessionManager: SessionManager

    // Colores del menu
    private val COLOR_ACTIVE_BG = Color.parseColor("#F0FDF4")
    private val COLOR_INACTIVE_BG = Color.WHITE
    private val COLOR_ACTIVE_TEXT = Color.parseColor("#047857")
    private val COLOR_INACTIVE_TEXT = Color.parseColor("#111827")

    // colores segun la categoria
    private val categoriaColores = mapOf(
        "Urgente" to "#EF4444",
        "Mantenimiento" to "#FBBF24",
        "Evento" to "#10B981",
        "Información" to "#4488EF"
    )

    private val menuItemsToHighlight = listOf(
        R.id.llInicio, R.id.llReportesMenu, R.id.llAccesosMenu, R.id.llChatMenu,
        R.id.llMapaMenu, R.id.llServiciosMenu, R.id.llAvisosMenu, R.id.llPerfilMenu
    )
    private var selectableItemBackground: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupStatusBar()
        setContentView(R.layout.activity_avisos_vecinales)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        avisosController = AvisosController()

        drawerLayout = findViewById(R.id.drawer_layout)
        llAvisosContainer = findViewById(R.id.llAvisosContainer)

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

        highlightActiveMenuItem(R.id.llAvisosMenu)

        cargarAvisos()

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

    private fun cargarAvisos() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val avisos = avisosController.obtenerAvisos()
                mostrarAvisosEnUI(avisos)
            } catch (e: Exception) {
                mostrarError("Error al cargar avisos")
            }
        }
    }

    private fun mostrarAvisosEnUI(avisos: List<Aviso>) {
        llAvisosContainer.removeAllViews()

        if (avisos.isEmpty()) {
            mostrarMensajeSinAvisos()
            return
        }

        avisos.forEach { aviso ->
            val avisoView = crearVistaAviso(aviso)
            llAvisosContainer.addView(avisoView)
        }
    }

    private fun mostrarMensajeSinAvisos() {
        val inflater = LayoutInflater.from(this)
        val mensajeView = inflater.inflate(R.layout.item_aviso, llAvisosContainer, false) as androidx.cardview.widget.CardView

        // Ocultar el borde lateral
        val bordeLateral = mensajeView.findViewById<View>(R.id.bordeLateral)
        bordeLateral.visibility = View.GONE

        // texto del mensaje
        val tvTipo = mensajeView.findViewById<TextView>(R.id.tvTipo)
        val tvFecha = mensajeView.findViewById<TextView>(R.id.tvFecha)
        val tvTitulo = mensajeView.findViewById<TextView>(R.id.tvTitulo)
        val tvDescripcion = mensajeView.findViewById<TextView>(R.id.tvDescripcion)

        tvTipo.text = "Sin avisos"
        tvTipo.setTextColor(Color.parseColor("#9CA3AF"))
        tvFecha.visibility = View.GONE
        tvTitulo.visibility = View.GONE
        tvDescripcion.text = "No hay avisos disponibles en este momento"
        tvDescripcion.setTextColor(Color.parseColor("#9CA3AF"))
        tvDescripcion.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        llAvisosContainer.addView(mensajeView)
    }

    private fun mostrarError(mensaje: String) {
        llAvisosContainer.removeAllViews()
        val errorView = LayoutInflater.from(this).inflate(R.layout.item_aviso, llAvisosContainer, false) as androidx.cardview.widget.CardView

        // Ocultar el borde lateral
        val bordeLateral = errorView.findViewById<View>(R.id.bordeLateral)
        bordeLateral.visibility = View.GONE

        // texto del error
        val tvTipo = errorView.findViewById<TextView>(R.id.tvTipo)
        val tvFecha = errorView.findViewById<TextView>(R.id.tvFecha)
        val tvTitulo = errorView.findViewById<TextView>(R.id.tvTitulo)
        val tvDescripcion = errorView.findViewById<TextView>(R.id.tvDescripcion)

        tvTipo.text = "Error"
        tvTipo.setTextColor(Color.parseColor("#EF4444"))
        tvFecha.visibility = View.GONE
        tvTitulo.visibility = View.GONE
        tvDescripcion.text = mensaje
        tvDescripcion.setTextColor(Color.parseColor("#EF4444"))
        tvDescripcion.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        llAvisosContainer.addView(errorView)
    }

    private fun crearVistaAviso(aviso: Aviso): androidx.cardview.widget.CardView {
        val inflater = LayoutInflater.from(this)
        val avisoView = inflater.inflate(R.layout.item_aviso, null) as androidx.cardview.widget.CardView

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 20)
        avisoView.layoutParams = layoutParams

        //colores segun la categoria
        val color = categoriaColores[aviso.categoriaNombre] ?: "#4488EF"

        // Borde lateral
        val bordeLateral = avisoView.findViewById<View>(R.id.bordeLateral)
        bordeLateral.setBackgroundColor(Color.parseColor(color))

        // Textos
        val tvTipo = avisoView.findViewById<TextView>(R.id.tvTipo)
        val tvFecha = avisoView.findViewById<TextView>(R.id.tvFecha)
        val tvTitulo = avisoView.findViewById<TextView>(R.id.tvTitulo)
        val tvDescripcion = avisoView.findViewById<TextView>(R.id.tvDescripcion)

        tvTipo.text = aviso.categoriaNombre
        tvTipo.setTextColor(Color.parseColor(color))

        // Titulo y descripcion
        tvTitulo.text = aviso.titulo
        tvDescripcion.text = aviso.descripcion

        // MOSTRAR FECHA DEL EVENTO SI TIENE
        if (aviso.fechaEvento != null && aviso.fechaEvento.isNotEmpty() && aviso.fechaEvento != "0000-00-00T00:00:00") {
            tvFecha.text = formatearFecha(aviso.fechaEvento)
            tvFecha.visibility = View.VISIBLE
        } else {
            tvFecha.visibility = View.GONE
        }

        return avisoView
    }

    private fun formatearFecha(fechaString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(fechaString)
            outputFormat.format(date ?: return fechaString)
        } catch (e: Exception) {
            fechaString
        }
    }

    // menu
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

        // Actualizar encabezado del menu
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

        llAvisos.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }

        llPerfil.setOnClickListener { navigateAndFinish(Perfil::class.java) }

        llCerrarSesion.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            showLogoutConfirmation()
        }
    }

//Metodo para actualizar el encabezado del menu
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