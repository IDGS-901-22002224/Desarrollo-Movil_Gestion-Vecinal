package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import android.graphics.drawable.Drawable
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import org.utl.reddeseguridadvecinal.util.SessionManager
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment

class Home : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // Colores para el resaltado del menu
    private val COLOR_ACTIVE_BG = Color.parseColor("#F0FDF4") // Fondo verde claro
    private val COLOR_INACTIVE_BG = Color.WHITE // Fondo blanco
    private val COLOR_ACTIVE_TEXT = Color.parseColor("#047857") // Texto e icono verde oscuro
    private val COLOR_INACTIVE_TEXT = Color.parseColor("#111827") // Texto e icono negro

    // items de menu
    private val menuItemsToHighlight = listOf(
        R.id.llInicio,
        R.id.llReportesMenu,
        R.id.llAccesosMenu,
        R.id.llChatMenu,
        R.id.llMapaMenu,
        R.id.llServiciosMenu,
        R.id.llAvisosMenu,
        R.id.llPerfilMenu
    )

    // almacenar el drawable seleccionable
    private var selectableItemBackground: Drawable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // VERIFICAR SI HAY SESION ACTIVA
        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
           // println("No hay sesion activa")
            val intent = Intent(this, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        setupStatusBar()

        setContentView(R.layout.activity_home)

        // DATOS DEL USUARIO
        val nombreCompleto = sessionManager.getApellidosCompletos()

        val tvNombreUsuario = findViewById<TextView>(R.id.tvNombreUsuario)
        if (nombreCompleto.isNotEmpty()) {
            tvNombreUsuario.text = nombreCompleto
            // println("HOLA $nombreCompleto")
        } else {
            tvNombreUsuario.text = "Usuario"
        }

        drawerLayout = findViewById(R.id.drawer_layout)

        // Inicializar el drawable seleccionable
        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDrawerMenuButton()
        setupGridButtonListeners()
        setupDrawerItemListeners()

        //resaltado a inicio
        highlightActiveMenuItem(R.id.llInicio)

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

    //Barra de estado
    private fun setupStatusBar() {
        window.statusBarColor = Color.parseColor("#F5F5F5") // Color del fondo

        // iconos de notificaciones oscuros
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }
    }

    private fun setupDrawerMenuButton() {
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }


    // Apartado del menu activo y desactivado
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

        fun navigateAndHighlight(targetActivity: Class<*>, activeLayoutId: Int) {
            highlightActiveMenuItem(activeLayoutId)
            drawerLayout.closeDrawer(GravityCompat.START)
            if (targetActivity != Home::class.java) {
                startActivity(Intent(this, targetActivity))
            }
        }

        llInicio.setOnClickListener { navigateAndHighlight(Home::class.java, R.id.llInicio) }
        llReportes.setOnClickListener { navigateAndHighlight(Reportes::class.java, R.id.llReportesMenu) }
        llAccesos.setOnClickListener { navigateAndHighlight(Acceso::class.java, R.id.llAccesosMenu) }
        llChat.setOnClickListener { navigateAndHighlight(Chat_vecinal::class.java, R.id.llChatMenu) }
        llMapa.setOnClickListener { navigateAndHighlight(Mapa::class.java, R.id.llMapaMenu) }
        llPerfil.setOnClickListener { navigateAndHighlight(Perfil::class.java, R.id.llPerfilMenu) }
        llServicios.setOnClickListener { navigateAndHighlight(Pagos_Servicios::class.java, R.id.llServiciosMenu) }
        llAvisos.setOnClickListener { navigateAndHighlight(Avisos_vecinales::class.java, R.id.llAvisosMenu) }

        // Cerrar sesion
        llCerrarSesion.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            showLogoutConfirmation()
        }
    }


    //Metodo para actualizar el encabezado con apellidos y direccion
    private fun updateDrawerHeader() {
        val sessionManager = SessionManager(this)
        val apellidos = sessionManager.getApellidosCompletos()
        val direccion = sessionManager.getDireccionCompleta()

        val navDrawerContent = findViewById<LinearLayout>(R.id.nav_drawer_content)
        val headerLayout = navDrawerContent.getChildAt(0) as LinearLayout

        val tvNombreUsuario = headerLayout.getChildAt(0) as? TextView
        val tvCasa = headerLayout.getChildAt(1) as? TextView

        tvNombreUsuario?.text = apellidos
        tvCasa?.text = direccion

        //println("Datos actualizados: $apellidos")
    }
    private fun showLogoutConfirmation() {
        val dialogFragment = ConfirmDialogFragment.newInstance(
            titulo = "CERRAR SESIÓN",
            mensajePrincipal = "¿Estás seguro de que quieres cerrar sesión?",
            mensajeSecundario = "Tendras que volver a iniciar sesión para reingresar",
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

        //println("[Home] Sesión cerrada")
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setupGridButtonListeners() {
        val btnReportes = findViewById<CardView>(R.id.btnReportes)
        val btnAccesos = findViewById<CardView>(R.id.btnAccesos)
        val btnChat = findViewById<CardView>(R.id.btnChat)
        val btnMapa = findViewById<CardView>(R.id.btnMapa)
        val btnServicios = findViewById<CardView>(R.id.btnServicios)
        val btnAvisos = findViewById<CardView>(R.id.btnAvisos)
        val btnEmergencia = findViewById<CardView>(R.id.btnEmergencia)

        btnReportes.setOnClickListener { startActivity(Intent(this, Reportes::class.java)) }
        btnAccesos.setOnClickListener { startActivity(Intent(this, Acceso::class.java)) }
        btnChat.setOnClickListener { startActivity(Intent(this, Chat_vecinal::class.java)) }
        btnMapa.setOnClickListener { startActivity(Intent(this, Mapa::class.java)) }
        btnAvisos.setOnClickListener { startActivity(Intent(this, Avisos_vecinales::class.java)) }
        btnServicios.setOnClickListener { startActivity(Intent(this, Pagos_Servicios::class.java)) }

        // btnEmergencia.setOnClickListener { startActivity(Intent(this, Emergencia::class.java)) }
    }
}