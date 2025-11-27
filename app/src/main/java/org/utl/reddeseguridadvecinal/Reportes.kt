package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
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
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.utl.reddeseguridadvecinal.controller.ReportesController
import org.utl.reddeseguridadvecinal.modelo.ReporteResponse
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class Reportes : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout


    private lateinit var llReportesContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvListaVacia: TextView

    private lateinit var sessionManager: SessionManager
    private val reportesController = ReportesController()

    // Formatos y Colores
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val outputFormat = SimpleDateFormat("dd/MM/yyyy - h:mm a", Locale.getDefault())
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
        window.statusBarColor = Color.parseColor("#F5F5F5")
        setContentView(R.layout.activity_reportes)

        sessionManager = SessionManager(this)
        initViews()

        setupDrawerMenuButton()
        setupDrawerItemListeners()
        setupReporteButtonListener()
        setupDrawerHeader()

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
        cargarReportes()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)


        llReportesContainer = findViewById(R.id.llReportesContainer)
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

    private fun cargarReportes() {
        progressBar.visibility = View.VISIBLE
        tvListaVacia.visibility = View.GONE
        llReportesContainer.removeAllViews()

        lifecycleScope.launch {
            val listaReportes = reportesController.getReportes()
            progressBar.visibility = View.GONE

            if (listaReportes != null) {
                if (listaReportes.isNotEmpty()) {
                    llenarListaManual(listaReportes.sortedByDescending { it.fechaCreacion })
                } else {
                    tvListaVacia.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(this@Reportes, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun llenarListaManual(reportes: List<ReporteResponse>) {
        // 1. ¡ESTA LÍNEA ES LA CLAVE!
        // Borra todo lo que había antes para no duplicar
        llReportesContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)

        for (reporte in reportes) {
            // 2. Inflar el diseño de la tarjeta
            val itemView = inflater.inflate(R.layout.item_reporte, llReportesContainer, false)

            // 3. Referencias
            val vEstadoColor = itemView.findViewById<View>(R.id.vEstadoColor)
            val ivEstadoIcono = itemView.findViewById<ImageView>(R.id.ivEstadoIcono)
            val tvNombreUsuario = itemView.findViewById<TextView>(R.id.tvNombreUsuario)
            val tvReporteUbicacion = itemView.findViewById<TextView>(R.id.tvReporteUbicacion)
            val tvReporteFecha = itemView.findViewById<TextView>(R.id.tvReporteFecha)
            val tvReporteDescripcion = itemView.findViewById<TextView>(R.id.tvReporteDescripcion)
            val cvReporteTipoContainer = itemView.findViewById<CardView>(R.id.cvReporteTipoContainer)
            val tvReporteTipoLabel = itemView.findViewById<TextView>(R.id.tvReporteTipoLabel)
            val ivReporteImagen = itemView.findViewById<ImageView>(R.id.ivReporteImagen)

            // 4. Llenar datos
            tvNombreUsuario.text = reporte.nombreUsuario
            tvReporteUbicacion.text = reporte.direccionTexto.ifEmpty { "Sin ubicación" }
            tvReporteDescripcion.text = reporte.descripcion
            tvReporteFecha.text = formatarFecha(reporte.fechaCreacion)

            // Lógica Visto/No Visto
            if (reporte.visto) {
                val colorVisto = Color.parseColor("#FBBF24")
                vEstadoColor.setBackgroundColor(colorVisto)
                ivEstadoIcono.setImageResource(R.drawable.ic_visibility)
                ivEstadoIcono.setColorFilter(colorVisto)
            } else {
                val colorNoVisto = Color.parseColor("#EF4444")
                vEstadoColor.setBackgroundColor(colorNoVisto)
                ivEstadoIcono.setImageResource(R.drawable.ic_visibility_off)
                ivEstadoIcono.setColorFilter(colorNoVisto)
            }

            // Lógica Tipo y Color
            val (colorFondo, colorTexto) = getColorForTipo(reporte.tipoReporteID)
            tvReporteTipoLabel.text = getNombreTipoPorId(reporte.tipoReporteID)
            cvReporteTipoContainer.setCardBackgroundColor(Color.parseColor(colorFondo))
            tvReporteTipoLabel.setTextColor(Color.parseColor(colorTexto))

            // Lógica Imagen
            if (!reporte.imagenBase64.isNullOrEmpty()) {
                try {
                    val base64Clean = reporte.imagenBase64.split(",").last()
                    val decodedBytes = Base64.decode(base64Clean, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    ivReporteImagen.setImageBitmap(bitmap)
                    ivReporteImagen.visibility = View.VISIBLE
                } catch (e: Exception) {
                    ivReporteImagen.visibility = View.GONE
                }
            } else {
                ivReporteImagen.visibility = View.GONE
            }

            // 5. Agregar la tarjeta al contenedor
            llReportesContainer.addView(itemView)
        }
    }

    private fun formatarFecha(fechaString: String): String {
        return try {
            val fecha = inputFormat.parse(fechaString.split(".")[0])
            outputFormat.format(fecha!!)
        } catch (e: Exception) { "Fecha inválida" }
    }

    private fun getNombreTipoPorId(id: Int): String {
        return when(id) {
            1 -> "Robo"
            2 -> "Vandalismo"
            3 -> "Act. Sospechosa"
            4 -> "Falla Servicio"
            5 -> "Ruido Excesivo"
            6 -> "Bache/Obstaculo"
            else -> "Otro"
        }
    }

    private fun getColorForTipo(tipoId: Int): Pair<String, String> {
        return when (tipoId) {
            1 -> Pair("#FEE2E2", "#991B1B")
            2 -> Pair("#FEF3C7", "#92400E")
            3 -> Pair("#FFEDD5", "#9A3412")
            4 -> Pair("#DBEAFE", "#1E40AF")
            5 -> Pair("#E0E7FF", "#3730A3")
            6 -> Pair("#D1FAE5", "#065F46")
            else -> Pair("#E5E7EB", "#374151")
        }
    }

    // ... (Resto de tus funciones de menú y drawers que ya tenías) ...
    private fun setupReporteButtonListener() { findViewById<CardView>(R.id.btnLevantarReporte).setOnClickListener { startActivity(Intent(this, Realizar_reporte::class.java)) } }
    private fun setupDrawerMenuButton() { findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) } }
    private fun setupDrawerHeader() {
        val tvDrawerName = findViewById<TextView>(R.id.tvDrawerUserName)
        val tvDrawerAddress = findViewById<TextView>(R.id.tvDrawerUserAddress)
        if (sessionManager.getApellidosCompletos().isNotEmpty()) tvDrawerName.text = sessionManager.getApellidosCompletos() else tvDrawerName.text = "Usuario"
        tvDrawerAddress.text = sessionManager.getDireccionCompleta()
    }
    private fun setupStatusBar() {
        window.statusBarColor = Color.parseColor("#F5F5F5")
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }
    private fun highlightActiveMenuItem(id: Int) { /* (Tu lógica de menú) */ }
    private fun setupDrawerItemListeners() { /* (Tu lógica de listeners) */ }
}
