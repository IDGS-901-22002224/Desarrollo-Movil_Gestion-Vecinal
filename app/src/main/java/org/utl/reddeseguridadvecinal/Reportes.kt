package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import androidx.core.view.WindowCompat

class Reportes : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // Colores y lista de ítems (Copiado de Home para consistencia)
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
        setContentView(R.layout.activity_reportes)

        // 1. Inicializar DrawerLayout y obtener Drawable seguro
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

        // 2. Configuración de listeners
        setupDrawerMenuButton()
        setupDrawerItemListeners()
        setupReporteButtonListener()

        // 🚨 RESALTADO: Asegurar que "Reportes" esté activo al cargar
        highlightActiveMenuItem(R.id.llReportesMenu)

        // 3. Manejo del botón de retroceso moderno
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

    // --- Lógica Específica de Reportes ---
    private fun setupReporteButtonListener() {
        // Enlaza el botón flotante a la actividad Realizar_reporte
        val btnLevantarReporte = findViewById<CardView>(R.id.btnLevantarReporte)
        btnLevantarReporte.setOnClickListener {
            val intent = Intent(this, Realizar_reporte::class.java)
            startActivity(intent)
        }
    }

    // --- Lógica Compartida del Drawer ---

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

    /**
     * Resalta el elemento de menú activo y desactiva el resto, basado en el ID.
     */
    private fun highlightActiveMenuItem(activeLayoutId: Int) {
        val navDrawerContent = findViewById<LinearLayout>(R.id.nav_drawer_content)
        val menuContainer = navDrawerContent.getChildAt(1) as LinearLayout

        for (i in 0 until menuContainer.childCount) {
            val child = menuContainer.getChildAt(i)

            if (child is LinearLayout && menuItemsToHighlight.contains(child.id)) {

                val isActive = child.id == activeLayoutId

                // 1. Configurar el fondo y el efecto de clic
                child.setBackgroundColor(if (isActive) COLOR_ACTIVE_BG else COLOR_INACTIVE_BG)
                child.foreground = if (!isActive) selectableItemBackground else null

                // 2. Configurar el texto y el icono
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
        // Enlazar todos los ítems del menú lateral
        val llInicio = findViewById<LinearLayout>(R.id.llInicio)
        val llReportes = findViewById<LinearLayout>(R.id.llReportesMenu)
        val llAccesos = findViewById<LinearLayout>(R.id.llAccesosMenu)
        val llChat = findViewById<LinearLayout>(R.id.llChatMenu)
        val llMapa = findViewById<LinearLayout>(R.id.llMapaMenu)
        val llServicios = findViewById<LinearLayout>(R.id.llServiciosMenu)
        val llAvisos = findViewById<LinearLayout>(R.id.llAvisosMenu)
        val llPerfil = findViewById<LinearLayout>(R.id.llPerfilMenu)
        val llCerrarSesion = findViewById<LinearLayout>(R.id.llCerrarSesion)

        // Función de ayuda para navegar (usado solo si salimos de Reportes)
        fun navigateAndFinish(targetActivity: Class<*>) {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, targetActivity))
            // Finalizar esta Activity para que al presionar 'Atrás' el usuario no regrese a Reportes
            finish()
        }

        // --- Lógica de Navegación del Menú ---
        llInicio.setOnClickListener { navigateAndFinish(Home::class.java) }

        // Reportes: Si ya estamos aquí, solo cierra el menú
        llReportes.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }

        llInicio.setOnClickListener { navigateAndFinish(Home::class.java) }
        llReportes.setOnClickListener { navigateAndFinish(Reportes::class.java) }
        llAccesos.setOnClickListener { navigateAndFinish(Acceso::class.java) }
        llChat.setOnClickListener { navigateAndFinish(Chat_vecinal::class.java) }
        llMapa.setOnClickListener { navigateAndFinish(Mapa::class.java) }
        llServicios.setOnClickListener { navigateAndFinish(Pagos_Servicios::class.java) }
        llAvisos.setOnClickListener { navigateAndFinish(Avisos_vecinales::class.java) }
        llPerfil.setOnClickListener { navigateAndFinish(Perfil::class.java) }

        // CERRAR SESIÓN (Lógica especial)
        llCerrarSesion.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}