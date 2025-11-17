package org.utl.reddeseguridadvecinal

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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

class Perfil : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var selectableItemBackground: Drawable? = null

    // Colores del menu lateral
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
        setContentView(R.layout.activity_perfil)

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
        highlightActiveMenuItem(R.id.llPerfilMenu)


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

        llInicio.setOnClickListener { navigateAndFinish(Home::class.java) }
        llReportes.setOnClickListener { navigateAndFinish(Reportes::class.java) }
        llAccesos.setOnClickListener { navigateAndFinish(Acceso::class.java) }
        llChat.setOnClickListener { navigateAndFinish(Chat_vecinal::class.java) }
        llMapa.setOnClickListener { navigateAndFinish(Mapa::class.java) }
        llServicios.setOnClickListener { navigateAndFinish(Pagos_Servicios::class.java) }
        llAvisos.setOnClickListener { navigateAndFinish(Avisos_vecinales::class.java) }

        llPerfil.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }

        llCerrarSesion.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    // --- OCULTAR TECLADO ---
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}
