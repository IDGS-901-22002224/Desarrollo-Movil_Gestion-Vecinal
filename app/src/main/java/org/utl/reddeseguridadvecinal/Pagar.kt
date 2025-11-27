package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.utl.reddeseguridadvecinal.dialogs.ConfirmDialogFragment
import org.utl.reddeseguridadvecinal.controller.SeleccionCargosDialogFragment
import org.utl.reddeseguridadvecinal.logica.PagarLogica
import org.utl.reddeseguridadvecinal.modelo.CargoMantenimientoDTO
import org.utl.reddeseguridadvecinal.modelo.CargoServicioDTO
import org.utl.reddeseguridadvecinal.modelo.CuentaUsuarioResponse
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class Pagar : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: SessionManager
    private lateinit var pagarLogica: PagarLogica
    private var selectableItemBackground: Drawable? = null

    private lateinit var tvEsteMesViviendas: TextView
    private lateinit var tvAdeudosViviendas: TextView
    private lateinit var tvTotalViviendas: TextView
    private lateinit var tvEsteMesServicios: TextView
    private lateinit var tvAdeudosServicios: TextView
    private lateinit var tvTotalServicios: TextView
    private lateinit var btnConfirmarViviendas: CardView
    private lateinit var btnConfirmarServicios: CardView
    private lateinit var btnAbonarViviendas: CardView
    private lateinit var btnAbonarServicios: CardView

    private var datosCuenta: CuentaUsuarioResponse? = null

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
        setContentView(R.layout.activity_pagar)

        sessionManager = SessionManager(this)
        pagarLogica = PagarLogica()

        drawerLayout = findViewById(R.id.drawer_layout)
        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        initViews()
        setupDrawerMenuButton()
        setupDrawerItemListeners()
        setupButtonListeners()

        highlightActiveMenuItem(R.id.llServiciosMenu)
        updateDrawerHeader()

        cargarDatosCuenta()

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

    private fun initViews() {
        // Viviendas
        tvEsteMesViviendas = findViewById(R.id.tvEsteMesViviendas)
        tvAdeudosViviendas = findViewById(R.id.tvAdeudosViviendas)
        tvTotalViviendas = findViewById(R.id.tvTotalViviendas)
        btnConfirmarViviendas = findViewById(R.id.btnConfirmarViviendas)
        btnAbonarViviendas = findViewById(R.id.btnAbonarViviendas)

        // Servicios
        tvEsteMesServicios = findViewById(R.id.tvEsteMesServicios)
        tvAdeudosServicios = findViewById(R.id.tvAdeudosServicios)
        tvTotalServicios = findViewById(R.id.tvTotalServicios)
        btnConfirmarServicios = findViewById(R.id.btnConfirmarServicios)
        btnAbonarServicios = findViewById(R.id.btnAbonarServicios)
    }

    private fun setupButtonListeners() {
        // Botones de CONFIRMAR
        btnConfirmarViviendas.setOnClickListener {
            datosCuenta?.let { cuenta ->
                val cargosPendientes = cuenta.cargosMantenimiento.filter { it.saldoPendiente > 0 }
                if (cargosPendientes.isNotEmpty()) {
                    mostrarConfirmacionPago(
                        tipo = "mantenimiento",
                        montoTotal = cargosPendientes.sumOf { it.saldoPendiente },
                        cargosMantenimiento = cargosPendientes,
                        cargosServicios = emptyList()
                    )
                } else {
                    Toast.makeText(this, "No hay cargos pendientes de mantenimiento", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnConfirmarServicios.setOnClickListener {
            datosCuenta?.let { cuenta ->
                val cargosPendientes = cuenta.cargosServicios.filter { it.saldoPendiente > 0 }
                if (cargosPendientes.isNotEmpty()) {
                    mostrarConfirmacionPago(
                        tipo = "servicios",
                        montoTotal = cargosPendientes.sumOf { it.saldoPendiente },
                        cargosMantenimiento = emptyList(),
                        cargosServicios = cargosPendientes
                    )
                } else {
                    Toast.makeText(this, "No hay cargos pendientes de servicios", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Botones de ABONAR
        btnAbonarViviendas.setOnClickListener {
            mostrarDialogSeleccionCargos("mantenimiento")
        }

        btnAbonarServicios.setOnClickListener {
            mostrarDialogSeleccionCargos("servicios")
        }
    }

    private fun mostrarDialogSeleccionCargos(tipo: String) {
        datosCuenta?.let { cuenta ->
            val cargosMantenimiento = if (tipo == "mantenimiento")
                cuenta.cargosMantenimiento.filter { it.saldoPendiente > 0 } else emptyList()
            val cargosServicios = if (tipo == "servicios")
                cuenta.cargosServicios.filter { it.saldoPendiente > 0 } else emptyList()

            if (cargosMantenimiento.isNotEmpty() || cargosServicios.isNotEmpty()) {
                val dialog = SeleccionCargosDialogFragment.newInstance(
                    tipo = tipo,
                    cargosMantenimiento = cargosMantenimiento,
                    cargosServicios = cargosServicios,
                    listener = object : SeleccionCargosDialogFragment.OnCargosSeleccionadosListener {
                        override fun onCargosSeleccionados(
                            cargosMantenimiento: List<CargoMantenimientoDTO>,
                            cargosServicios: List<CargoServicioDTO>,
                            montoTotal: Double,
                            cvv: String // Nuevo parámetro
                        ) {
                            procesarAbonoParcial(cargosMantenimiento, cargosServicios, montoTotal, cvv)
                        }
                    }
                )
                dialog.show(supportFragmentManager, "SeleccionCargosDialog")
            } else {
                Toast.makeText(this, "No hay cargos pendientes para abonar", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Cargando datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun procesarAbonoParcial(
        cargosMantenimiento: List<CargoMantenimientoDTO>,
        cargosServicios: List<CargoServicioDTO>,
        montoTotal: Double,
        cvv: String
    ) {

        // confirmación antes de proceder
        val tipoTexto = when {
            cargosMantenimiento.isNotEmpty() && cargosServicios.isNotEmpty() -> "mantenimiento y servicios"
            cargosMantenimiento.isNotEmpty() -> "mantenimiento"
            cargosServicios.isNotEmpty() -> "servicios"
            else -> "cargos"
        }

        val dialogFragment = ConfirmDialogFragment.newInstance(
            titulo = "CONFIRMAR ABONO",
            mensajePrincipal = "¿Estás seguro de que quieres realizar el abono de $tipoTexto?",
            mensajeSecundario = "Total a abonar: ${formatearMoneda(montoTotal)}",
            textoBotonConfirmar = " Abonar ",
            textoBotonCancelar = "Cancelar",
            onConfirm = {
                realizarAbonoParcial(cargosMantenimiento, cargosServicios, montoTotal, cvv)
            }
        )
        dialogFragment.show(supportFragmentManager, "AbonoConfirmDialog")
    }

    private fun realizarAbonoParcial(
        cargosMantenimiento: List<CargoMantenimientoDTO>,
        cargosServicios: List<CargoServicioDTO>,
        montoTotal: Double,
        cvv: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val usuarioId = sessionManager.getUsuarioID()
                val pdfBytes = withContext(Dispatchers.IO) {
                    pagarLogica.realizarAbonoParcial(
                        usuarioId,
                        cargosMantenimiento,
                        cargosServicios,
                        montoTotal,
                        cvv
                    )
                }

                if (pdfBytes != null) {
                    // Guardar el PDF
                    val archivoGuardado = guardarPDF(pdfBytes, "comprobante_abono_${System.currentTimeMillis()}.pdf")
                    if (archivoGuardado != null) {
                        abrirPDF(archivoGuardado)
                        Toast.makeText(this@Pagar, "Abono realizado exitosamente", Toast.LENGTH_LONG).show()
                        // Recargar datos para actualizar
                        cargarDatosCuenta()
                    } else {
                        Toast.makeText(this@Pagar, "Error al guardar el comprobante", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Pagar, "Error al realizar el abono", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@Pagar, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarConfirmacionPago(
        tipo: String,
        montoTotal: Double,
        cargosMantenimiento: List<CargoMantenimientoDTO>,
        cargosServicios: List<CargoServicioDTO>
    ) {
        val tipoTexto = if (tipo == "mantenimiento") "mantenimiento" else "servicios"
        val dialogFragment = ConfirmDialogFragment.newInstance(
            titulo = "CONFIRMAR PAGO",
            mensajePrincipal = "¿Estás seguro de que quieres realizar el pago completo de $tipoTexto?",
            mensajeSecundario = "Total a pagar: ${formatearMoneda(montoTotal)}",
            textoBotonConfirmar = " Pagar ",
            textoBotonCancelar = "Cancelar",
            onConfirm = {
                realizarPago(tipo, cargosMantenimiento, cargosServicios)
            }
        )
        dialogFragment.show(supportFragmentManager, "PagoConfirmDialog")
    }

    private fun realizarPago(
        tipo: String,
        cargosMantenimiento: List<CargoMantenimientoDTO>,
        cargosServicios: List<CargoServicioDTO>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val usuarioId = sessionManager.getUsuarioID()
                val pdfBytes = withContext(Dispatchers.IO) {
                    if (tipo == "mantenimiento") {
                        pagarLogica.realizarPagoMantenimiento(usuarioId, cargosMantenimiento)
                    } else {
                        pagarLogica.realizarPagoServicios(usuarioId, cargosServicios)
                    }
                }

                if (pdfBytes != null) {
                    // Guardar el PDF
                    val archivoGuardado = guardarPDF(pdfBytes, "comprobante_pago_${System.currentTimeMillis()}.pdf")
                    if (archivoGuardado != null) {
                        abrirPDF(archivoGuardado)
                        Toast.makeText(this@Pagar, "Pago realizado exitosamente", Toast.LENGTH_LONG).show()
                        // Recargar datos
                        cargarDatosCuenta()
                    } else {
                        Toast.makeText(this@Pagar, "Error al guardar el comprobante", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Pagar, "Error al realizar el pago", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@Pagar, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarPDF(pdfBytes: ByteArray, nombreArchivo: String): File? {
        return try {
            // Guardar en Descargas
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val archivo = File(downloadsDir, nombreArchivo)

            FileOutputStream(archivo).use { output ->
                output.write(pdfBytes)
            }

            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(archivo)
            sendBroadcast(mediaScanIntent)

            //println("PDF guardado en Descargas: ${archivo.absolutePath}")
            archivo
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: guardar en cache interno
            val archivoCache = File(cacheDir, nombreArchivo)
            FileOutputStream(archivoCache).use { output ->
                output.write(pdfBytes)
            }
            //println("PDF guardado en cache: ${archivoCache.absolutePath}")
            archivoCache
        }
    }

    private fun abrirPDF(archivo: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    this,
                    "org.utl.reddeseguridadvecinal.provider",
                    archivo
                )
            } else {
                Uri.fromFile(archivo)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                Toast.makeText(this, "PDF abierto correctamente", Toast.LENGTH_SHORT).show()
            } else {
                // Si no hay app para PDFs, mostrar dónde se guardó
                Toast.makeText(this,
                    "Pago exitoso. PDF guardado en: Descargas/${archivo.name}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this,
                "Pago exitoso. PDF guardado pero error al abrir: ${archivo.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cargarDatosCuenta() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val usuarioId = sessionManager.getUsuarioID()
                val cuenta = withContext(Dispatchers.IO) {
                    pagarLogica.obtenerDatosCuenta(usuarioId)
                }

                if (cuenta != null) {
                    datosCuenta = cuenta
                    actualizarUI(cuenta)
                } else {
                    Toast.makeText(this@Pagar, "Error al cargar los datos de la cuenta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@Pagar, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarUI(cuenta: CuentaUsuarioResponse) {
        // Calcular totales para mantenimiento
        val (esteMesMantenimiento, adeudosMantenimiento, totalMantenimiento) =
            pagarLogica.calcularTotalesMantenimiento(cuenta.cargosMantenimiento)

        // Calcular totales para servicios
        val (esteMesServicios, adeudosServicios, totalServicios) =
            pagarLogica.calcularTotalesServicios(cuenta.cargosServicios)

        tvEsteMesViviendas.text = "Este mes: ${formatearMoneda(esteMesMantenimiento)}"
        tvAdeudosViviendas.text = "Adeudos: ${formatearMoneda(adeudosMantenimiento)}"
        tvTotalViviendas.text = "Total a pagar: ${formatearMoneda(totalMantenimiento)}"

        tvEsteMesServicios.text = "Este mes: ${formatearMoneda(esteMesServicios)}"
        tvAdeudosServicios.text = "Adeudos: ${formatearMoneda(adeudosServicios)}"
        tvTotalServicios.text = "Total a pagar: ${formatearMoneda(totalServicios)}"
    }

    private fun formatearMoneda(monto: Double): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return formato.format(monto)
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
        pagarLogica.cerrarSesion(this)

        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}