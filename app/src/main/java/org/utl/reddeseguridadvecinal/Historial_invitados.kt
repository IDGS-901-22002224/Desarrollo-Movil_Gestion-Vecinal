package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.AccesosController
import org.utl.reddeseguridadvecinal.util.SessionManager

class Historial_invitados : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvHistorial: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvListaVacia: TextView
    private lateinit var adapter: InvitadosAdapter
    private lateinit var sessionManager: SessionManager
    private val accesosController = AccesosController()
    private var usuarioId: Int = -1

    // Colores y menu
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
        setContentView(R.layout.activity_historial_invitados)

        sessionManager = SessionManager(this)
        usuarioId = sessionManager.getUserId()

        // Inicializar Vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        rvHistorial = findViewById(R.id.rvHistorialInvitados)
        progressBar = findViewById(R.id.progressBar)
        tvListaVacia = findViewById(R.id.tvListaVacia)


        setupRecyclerView()
        cargarHistorial()

        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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

    private fun setupRecyclerView() {
        adapter = InvitadosAdapter(emptyList())
        rvHistorial.layoutManager = LinearLayoutManager(this)
        rvHistorial.adapter = adapter
    }

    private fun cargarHistorial() {
        progressBar.visibility = View.VISIBLE
        rvHistorial.visibility = View.GONE
        tvListaVacia.visibility = View.GONE

        lifecycleScope.launch {
            val historial = accesosController.getHistorialInvitados(usuarioId)

            progressBar.visibility = View.GONE

            if (historial != null) {
                if (historial.isNotEmpty()) {
                    rvHistorial.visibility = View.VISIBLE
                    adapter.actualizarLista(historial.sortedByDescending { it.fechaGeneracion })
                } else {
                    tvListaVacia.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(this@Historial_invitados, "Error al cargar el historial", Toast.LENGTH_SHORT).show()
                tvListaVacia.text = "Error de conexión"
                tvListaVacia.visibility = View.VISIBLE
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

        fun navigateAndFinish(targetActivity: Class<*>) {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, targetActivity))
            finish()
        }

        fun navigateAndHighlight(targetActivity: Class<*>, activeLayoutId: Int) {
            highlightActiveMenuItem(activeLayoutId)
            drawerLayout.closeDrawer(GravityCompat.START)
            if (targetActivity != Historial_invitados::class.java) {
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
