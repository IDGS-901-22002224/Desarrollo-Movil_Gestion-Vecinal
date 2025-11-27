package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.PagosServiciosController
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment
import org.utl.reddeseguridadvecinal.dialogs.ErrorDialogFragment
import org.utl.reddeseguridadvecinal.logica.PagosServiciosLogica
import org.utl.reddeseguridadvecinal.modelo.DatosGraficaPagos
import org.utl.reddeseguridadvecinal.modelo.DatosMesActual
import org.utl.reddeseguridadvecinal.util.SessionManager

class Pagos_Servicios : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: SessionManager
    private var selectableItemBackground: Drawable? = null
    private lateinit var pagosServiciosLogica: PagosServiciosLogica
    private lateinit var pagosServiciosController: PagosServiciosController

    private lateinit var flGraficoContainer: FrameLayout
    private lateinit var ivGraficoPastel: ImageView

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
        setContentView(R.layout.activity_pagos_servicios)

        sessionManager = SessionManager(this)
        pagosServiciosLogica = PagosServiciosLogica(this)
        pagosServiciosController = PagosServiciosController()

        flGraficoContainer = findViewById(R.id.flGraficoContainer)
        ivGraficoPastel = findViewById(R.id.ivGraficoPastel)

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

        cargarDatosGrafica()

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

    // --- METODOS COMPARTIDOS ---

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

        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        val btnPagos = findViewById<CardView>(R.id.btnPagos)
        val btnRecervas = findViewById<CardView>(R.id.btnRecervas)
        val btnServiciosInternos = findViewById<CardView>(R.id.btnServiciosInternos)

        updateDrawerHeader()

        //  Pagos
        btnPagos.setOnClickListener {
            verificarTarjetaYNavegar(Pagar::class.java)
        }

        //  Reservas de recintos
        btnRecervas.setOnClickListener {
            val intent = Intent(this, Historial_recinto::class.java)
            startActivity(intent)
        }

        //  Servicios internos
        btnServiciosInternos.setOnClickListener {
            verificarTarjetaYNavegar(Historial_servicios::class.java)
        }

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

        llServicios.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }

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
        pagosServiciosLogica.cerrarSesion(this)

        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun cargarDatosGrafica() {
        lifecycleScope.launch {
            try {
                val usuarioId = sessionManager.getUserId()
                val datosGrafica = pagosServiciosController.obtenerDatosParaGrafica(usuarioId)
                val datosMes = pagosServiciosController.obtenerDatosMesActual(usuarioId)

                if (datosGrafica != null) {
                    val bitmap = pagosServiciosLogica.crearGraficaPastel(datosGrafica)
                    ivGraficoPastel.setImageBitmap(bitmap)

                    actualizarLeyendas(datosGrafica, datosMes)
                } else {
                    mostrarGraficaPorDefecto()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mostrarGraficaPorDefecto()
            }
        }
    }

    private fun actualizarLeyendas(datos: DatosGraficaPagos, datosMes: DatosMesActual? = null) {
        val layoutVivienda = findViewById<LinearLayout>(R.id.llLeyendaVivienda)
        val layoutServicios = findViewById<LinearLayout>(R.id.llLeyendaServicios)

        val textViewVivienda = layoutVivienda.getChildAt(1) as TextView
        val textViewServicios = layoutServicios.getChildAt(1) as TextView

        textViewVivienda.text = pagosServiciosLogica.formatearTextoVivienda(datos, datosMes)
        textViewServicios.text = pagosServiciosLogica.formatearTextoServicios(datos, datosMes)
    }

    private fun mostrarGraficaPorDefecto() {
        ivGraficoPastel.setImageResource(R.drawable.ic_pie_chart_placeholder)

        val layoutVivienda = findViewById<LinearLayout>(R.id.llLeyendaVivienda)
        val layoutServicios = findViewById<LinearLayout>(R.id.llLeyendaServicios)

        val textViewVivienda = layoutVivienda.getChildAt(1) as TextView
        val textViewServicios = layoutServicios.getChildAt(1) as TextView

        textViewVivienda.text = "Pagos de vivienda: $0.00 (0%)"
        textViewServicios.text = "Pagos de servicios: $0.00 (0%)"
    }

    //Verificar tarjeta antes de navegar
    private fun verificarTarjetaYNavegar(destino: Class<*>) {
        lifecycleScope.launch {
            try {
                val usuarioId = sessionManager.getUserId()
                val tieneTarjeta = pagosServiciosLogica.verificarTarjetaRegistrada(usuarioId)

                if (tieneTarjeta) {
                    // Si tiene tarjeta
                    val intent = Intent(this@Pagos_Servicios, destino)
                    startActivity(intent)
                } else {
                    // Si NO tiene tarjeta
                    mostrarErrorTarjetaNoRegistrada()
                }
            } catch (e: Exception) {
                e.printStackTrace()

                mostrarErrorTarjetaNoRegistrada()
            }
        }
    }

    // Modal de error
    private fun mostrarErrorTarjetaNoRegistrada() {
        val dialogFragment = ErrorDialogFragment.newInstance(
            titulo = "TARJETA NO REGISTRADA",
            mensajePrincipal = "No tienes una tarjeta registrada para realizar pagos",
            mensajeSecundario = "Debes registrar una tarjeta en tu perfil para poder realizar pagos y abonos",
            textoBotonAceptar = "Ir a Perfil",
            onAceptar = {
                // Navegar a la pantalla de Perfil
                val intent = Intent(this@Pagos_Servicios, Perfil::class.java)
                startActivity(intent)
            }
        )
        dialogFragment.show(supportFragmentManager, "ErrorTarjetaDialog")
    }
}