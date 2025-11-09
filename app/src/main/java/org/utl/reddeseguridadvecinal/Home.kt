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
import androidx.core.view.WindowCompat

class Home : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // Colores para el resaltado visual (Definidos aquí para uso centralizado)
    private val COLOR_ACTIVE_BG = Color.parseColor("#F0FDF4") // Fondo verde muy claro
    private val COLOR_INACTIVE_BG = Color.WHITE // Fondo blanco (predeterminado)
    private val COLOR_ACTIVE_TEXT = Color.parseColor("#047857") // Texto/Ícono verde oscuro
    private val COLOR_INACTIVE_TEXT = Color.parseColor("#111827") // Texto/Ícono gris/negro

    // Lista de IDs de los ítems de menú (¡Ya corregidos en tu XML!)
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

    // Variable para almacenar el Drawable seleccionable una vez
    private var selectableItemBackground: Drawable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🎨 CONFIGURAR BARRA DE ESTADO
        setupStatusBar()

        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawer_layout)

        // Inicializar el Drawable seleccionable de forma segura
        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle() // Liberar TypedArray

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDrawerMenuButton()
        setupGridButtonListeners()
        setupDrawerItemListeners()

        // Aplicar el resaltado a 'Inicio' al cargar la aplicación
        highlightActiveMenuItem(R.id.llInicio)

        // Manejo moderno del botón de retroceso (OnBackPressedDispatcher)
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

    /**
     * Configura el color de la barra de estado para que coincida con el fondo de la app
     */
    private fun setupStatusBar() {
        window.statusBarColor = Color.parseColor("#F5F5F5") // Color del fondo de la app

        // Hacer que los iconos de la barra de estado sean oscuros (para fondo claro)
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

    /**
     * Resalta el elemento de menú activo y desactiva el resto, basado en el ID.
     */
    private fun highlightActiveMenuItem(activeLayoutId: Int) {
        val navDrawerContent = findViewById<LinearLayout>(R.id.nav_drawer_content)
        val menuContainer = navDrawerContent.getChildAt(1) as LinearLayout // 👈 Aquí está el truco

        for (i in 0 until menuContainer.childCount) {
            val child = menuContainer.getChildAt(i)

            if (child is LinearLayout && menuItemsToHighlight.contains(child.id)) {
                val isActive = child.id == activeLayoutId

                // Fondo y efecto clic
                child.setBackgroundColor(if (isActive) COLOR_ACTIVE_BG else COLOR_INACTIVE_BG)
                child.foreground = if (!isActive) selectableItemBackground else null

                // Texto e icono
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
        // Enlazar todos los ítems del menú lateral (usando los IDs corregidos)
        val llInicio = findViewById<LinearLayout>(R.id.llInicio)
        val llReportes = findViewById<LinearLayout>(R.id.llReportesMenu)
        val llAccesos = findViewById<LinearLayout>(R.id.llAccesosMenu)
        val llChat = findViewById<LinearLayout>(R.id.llChatMenu)
        val llMapa = findViewById<LinearLayout>(R.id.llMapaMenu)
        val llServicios = findViewById<LinearLayout>(R.id.llServiciosMenu)
        val llAvisos = findViewById<LinearLayout>(R.id.llAvisosMenu)
        val llPerfil = findViewById<LinearLayout>(R.id.llPerfilMenu)
        val llCerrarSesion = findViewById<LinearLayout>(R.id.llCerrarSesion)

        // Función de ayuda para navegar y aplicar resaltado
        fun navigateAndHighlight(targetActivity: Class<*>, activeLayoutId: Int) {
            highlightActiveMenuItem(activeLayoutId)
            drawerLayout.closeDrawer(GravityCompat.START)
            if (targetActivity != Home::class.java) {
                startActivity(Intent(this, targetActivity))
            }
        }

        // --- Lógica de Navegación del Menú ---
        llInicio.setOnClickListener { navigateAndHighlight(Home::class.java, R.id.llInicio) }
        llReportes.setOnClickListener { navigateAndHighlight(Reportes::class.java, R.id.llReportesMenu) }
        llAccesos.setOnClickListener { navigateAndHighlight(Acceso::class.java, R.id.llAccesosMenu) }
        llChat.setOnClickListener { navigateAndHighlight(Chat_vecinal::class.java, R.id.llChatMenu) }
        llMapa.setOnClickListener { navigateAndHighlight(Mapa::class.java, R.id.llMapaMenu) }
        llPerfil.setOnClickListener { navigateAndHighlight(Perfil::class.java, R.id.llPerfilMenu) }
        llServicios.setOnClickListener { navigateAndHighlight(Perfil::class.java, R.id.llServiciosMenu) }

        // Ítems faltantes en el código anterior
        llAvisos.setOnClickListener { navigateAndHighlight(Avisos_vecinales::class.java, R.id.llAvisosMenu) }

        // CERRAR SESIÓN (Lógica especial y limpieza de pila)
        llCerrarSesion.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
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
        // Agregando el listener de Emergencia:
        // btnEmergencia.setOnClickListener { startActivity(Intent(this, Emergencia::class.java)) }
    }
}