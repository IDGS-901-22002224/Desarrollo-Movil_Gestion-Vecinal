package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // ¡Importar CardView!
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class Acceso : AppCompatActivity() {

    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    private var selectableItemBackground: Drawable? = null

    // Colores iguales a Home/Reportes
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
        setContentView(R.layout.activity_acceso)

        // Mantengo tu listener de insets exactamente como estaba
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar DrawerLayout y selectable ripple
        drawerLayout = findViewById(R.id.drawer_layout)
        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        // Ajustes de statusbar (consistente con el resto)
        window.statusBarColor = Color.parseColor("#F5F5F5")
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        // Configurar listeners del drawer/menu
        setupDrawerMenuButton()
        setupDrawerItemListeners()

        // Resaltar el item activo (Accesos)
        highlightActiveMenuItem(R.id.llAccesosMenu)

        // Mantener tus listeners de botones (registro / historial)
        setupButtonListeners()

        // Manejo del botón de retroceso para cerrar el drawer si está abierto
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

    // Tu función existente (no la toqué)
    private fun setupButtonListeners() {
        // 1. Obtener la referencia al botón "Accesos para invitados"
        val btnRegistro = findViewById<CardView>(R.id.btnAccesosInvitados)

        // 2. Obtener la referencia al botón "Historial de invitados"
        val btnHistorial = findViewById<CardView>(R.id.btnHistorialInvitados)

        // --- Listener para Registro de Invitados ---
        btnRegistro.setOnClickListener {
            val intentRegistro = Intent(this, Registro_invitados::class.java)
            startActivity(intentRegistro)
        }

        // --- Listener para Historial de Invitados ---
        btnHistorial.setOnClickListener {
            val intentHistorial = Intent(this, Historial_invitados::class.java)
            startActivity(intentHistorial)
        }
    }

    // --- Drawer/Menu functions (añadidas) ---
    private fun setupDrawerMenuButton() {
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun highlightActiveMenuItem(activeLayoutId: Int) {
        // Nav drawer content (igual que en Home/Reportes)
        val navDrawerContent = findViewById<LinearLayout>(R.id.nav_drawer_content)
        val menuContainer = navDrawerContent.getChildAt(1) as LinearLayout

        for (i in 0 until menuContainer.childCount) {
            val child = menuContainer.getChildAt(i)
            if (child is LinearLayout && menuItemsToHighlight.contains(child.id)) {
                val isActive = child.id == activeLayoutId

                // Fondo y ripple
                child.setBackgroundColor(if (isActive) COLOR_ACTIVE_BG else COLOR_INACTIVE_BG)
                child.foreground = if (!isActive) selectableItemBackground else null

                // Icono + texto
                if (child.childCount >= 2) {
                    val icon = child.getChildAt(0) as android.widget.ImageView
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

        // Función auxiliar para navegar y cerrar drawer
        fun navigateAndFinish(targetActivity: Class<*>) {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, targetActivity))
            finish()
        }

        // Navegación del menú (mantengo la misma lógica)
        llInicio.setOnClickListener { navigateAndFinish(Home::class.java) }
        llReportes.setOnClickListener { navigateAndFinish(Reportes::class.java) }
        llAccesos.setOnClickListener { /* estamos en Acceso -> solo cerrar drawer */ drawerLayout.closeDrawer(GravityCompat.START) }
        llChat.setOnClickListener { navigateAndFinish(Chat_vecinal::class.java) }
        llMapa.setOnClickListener { navigateAndFinish(Mapa::class.java) }
        llServicios.setOnClickListener { navigateAndFinish(Pagos_Servicios::class.java) }
        llAvisos.setOnClickListener { navigateAndFinish(Avisos_vecinales::class.java) }
        llPerfil.setOnClickListener { navigateAndFinish(Perfil::class.java) }

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
