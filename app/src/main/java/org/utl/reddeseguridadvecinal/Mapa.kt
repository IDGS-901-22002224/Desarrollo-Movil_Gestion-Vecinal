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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.MapaController
import org.utl.reddeseguridadvecinal.modelo.MarcadorResponse
import org.utl.reddeseguridadvecinal.util.SessionManager

class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var drawerLayout: DrawerLayout
    private var selectableItemBackground: Drawable? = null

    // Variables para el Mapa y el Controlador
    private lateinit var googleMap: GoogleMap
    private val mapaController = MapaController()
    private lateinit var sessionManager: SessionManager


    private val COLOR_ACTIVE_BG = Color.parseColor("#F0FDF4")

    private val COLOR_INACTIVE_BG = Color.WHITE
    private val COLOR_ACTIVE_TEXT = Color.parseColor("#047857")
    private val COLOR_INACTIVE_TEXT = Color.parseColor("#111827")

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa)

        window.statusBarColor = Color.parseColor("#F5F5F5")
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        sessionManager = SessionManager(this) // Inicializar SessionManager

        // Inicializar fondo seleccionable
        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // INICIALIZAR EL MAPA
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupDrawerMenuButton()
        setupDrawerItemListeners()
        setupDrawerHeader()
        highlightActiveMenuItem(R.id.llMapaMenu)

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

    //ESTA FUNCION ES LLAMADA CUANDO EL MAPA ESTA LISTO
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 1. Mover la camara a una ubicación (Ej. para que no se abra y se muestre el mundo entero)
        val ubicacionInicial = LatLng(21.1523, -101.711)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 15f))

        // 2. Cargar los marcadores de la API
        cargarMarcadoresDeAPI()
    }

    //Llama al C# API y dibuja los marcadores
    private fun cargarMarcadoresDeAPI() {
        lifecycleScope.launch {
            val marcadores = mapaController.getMarcadores()

            if (marcadores != null) {
                // Exito: La lista de marcadores llego de la API
                for (marcador in marcadores) {

                    // 1. Crear la coordenada
                    val posicion = LatLng(marcador.latitud, marcador.longitud)

                    // 2. Crear el marcador
                    val markerOptions = MarkerOptions()
                        .position(posicion)
                        .title(marcador.indicador)     // El 'indicador' será el título
                        .snippet(marcador.comentario)  // El 'comentario' sera el texto extra

                    // 3. Poner un color diferente a cada marcador
                    markerOptions.icon(getMarkerIcon(marcador.indicador))

                    // 4. Añadir al mapa
                    googleMap.addMarker(markerOptions)
                }
            } else {
                Toast.makeText(this@Mapa, "No se pudieron cargar los marcadores", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para cambiar el color del pin
    private fun getMarkerIcon(indicador: String): com.google.android.gms.maps.model.BitmapDescriptor {
        val color = when (indicador.lowercase()) {
            "peligro" -> BitmapDescriptorFactory.HUE_RED
            "ayuda" -> BitmapDescriptorFactory.HUE_AZURE
            "obstaculo" -> BitmapDescriptorFactory.HUE_ORANGE
            "actividad" -> BitmapDescriptorFactory.HUE_YELLOW
            else -> BitmapDescriptorFactory.HUE_GREEN
        }
        return BitmapDescriptorFactory.defaultMarker(color)
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
            if (targetActivity != Mapa::class.java) {
                startActivity(Intent(this, targetActivity))
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