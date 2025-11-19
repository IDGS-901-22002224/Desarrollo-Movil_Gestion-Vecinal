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
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.ReportesController
import org.utl.reddeseguridadvecinal.util.SessionManager

class Reportes : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvReportes: RecyclerView
    private lateinit var reportesAdapter: ReportesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvListaVacia: TextView
    private lateinit var sessionManager: SessionManager
    private val reportesController = ReportesController()
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

        sessionManager = SessionManager(this)
        initViews()

        setupDrawerMenuButton()
        setupDrawerItemListeners()
        setupReporteButtonListener()
        setupDrawerHeader()
        setupRecyclerView()
        cargarReportes()
        highlightActiveMenuItem(R.id.llReportesMenu)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    val intent = Intent(this@Reportes, Home::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (::rvReportes.isInitialized) {
            cargarReportes()
        }
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        rvReportes = findViewById(R.id.rvReportes)
        progressBar = findViewById(R.id.progressBar)
        tvListaVacia = findViewById(R.id.tvListaVacia)

        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        val cvHeader = findViewById<CardView>(R.id.cvHeader)
        ViewCompat.setOnApplyWindowInsetsListener(cvHeader) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupRecyclerView() {
        reportesAdapter = ReportesAdapter(emptyList())
        rvReportes.adapter = reportesAdapter
        rvReportes.layoutManager = LinearLayoutManager(this)
    }

    private fun cargarReportes() {
        mostrarLoading(true)

        lifecycleScope.launch {
            val listaReportes = reportesController.getReportes()

            mostrarLoading(false)

            if (listaReportes == null) {
                Toast.makeText(this@Reportes, "Error al cargar reportes", Toast.LENGTH_SHORT).show()
                tvListaVacia.visibility = View.VISIBLE
                rvReportes.visibility = View.GONE
            } else if (listaReportes.isEmpty()) {
                tvListaVacia.visibility = View.VISIBLE
                rvReportes.visibility = View.GONE
            } else {
                tvListaVacia.visibility = View.GONE
                rvReportes.visibility = View.VISIBLE
                reportesAdapter.actualizarLista(listaReportes.sortedByDescending { it.fechaCreacion })
            }
        }
    }

    private fun mostrarLoading(estaCargando: Boolean) {
        if (estaCargando) {
            progressBar.visibility = View.VISIBLE
            rvReportes.visibility = View.GONE
            tvListaVacia.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    private fun setupReporteButtonListener() {
        val btnLevantarReporte = findViewById<CardView>(R.id.btnLevantarReporte)
        btnLevantarReporte.setOnClickListener {
            val intent = Intent(this, Realizar_reporte::class.java)
            startActivity(intent)
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

        fun navigateAndHighlight(targetActivity: Class<*>, activeLayoutId: Int) {
            highlightActiveMenuItem(activeLayoutId)
            drawerLayout.closeDrawer(GravityCompat.START)
            if (targetActivity != Reportes::class.java) {
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