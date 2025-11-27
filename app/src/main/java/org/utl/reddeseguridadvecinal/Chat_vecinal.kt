package org.utl.reddeseguridadvecinal

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.utl.reddeseguridadvecinal.modelo.MensajeChat
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Chat_vecinal : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageButton
    private lateinit var svMensajes: ScrollView
    private lateinit var etMensaje: EditText
    private lateinit var btnEnviar: CardView
    private lateinit var llMensajesContainer: LinearLayout
    private lateinit var database: DatabaseReference
    private var chatListener: ChildEventListener? = null
    private lateinit var chatRef: DatabaseReference
    private lateinit var sessionManager: SessionManager
    private var miUid: String? = null
    private var miNombreUsuario: String? = null
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

    private var selectableItemBackground: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_chat_vecinal)

        setupStatusBar()
        sessionManager = SessionManager(this)
        miUid = sessionManager.getFirebaseId()

        miNombreUsuario = sessionManager.getApellidosCompletos()
        if (miNombreUsuario.isNullOrEmpty()) {
            miNombreUsuario = "Usuario Desconocido"
        }

        database = FirebaseDatabase.getInstance("https://red-seguridad-vecinal-default-rtdb.firebaseio.com/").reference
        chatRef = database.child("mensajes_chat") 

        if (miUid == null || miUid == "") {
            Toast.makeText(this, "Error de sesión, por favor ingresa de nuevo", Toast.LENGTH_LONG).show()
            val intent = Intent(this, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return 
        }

        initViews()
        setupDrawerMenuButton()
        setupDrawerItemListeners()

        setupDrawerHeader()

        highlightActiveMenuItem(R.id.llChatMenu)

        setupChat()

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
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btnMenu)
        svMensajes = findViewById(R.id.svMensajes)

        etMensaje = findViewById(R.id.etMensaje)
        btnEnviar = findViewById(R.id.btnEnviar)
        llMensajesContainer = findViewById(R.id.llMensajesContainer)

        val typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackground = typedArray.getDrawable(0)
        typedArray.recycle()

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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

    private fun setupChat() {
        btnEnviar.setOnClickListener {
            enviarMensaje()
        }

        escucharMensajes()
    }

    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val netDate = Date(timestamp)
            sdf.format(netDate)
        } catch (e: Exception) {
            "---" 
        }
    }

    private fun enviarMensaje() {
        val textoMensaje = etMensaje.text.toString().trim()

        if (textoMensaje.isNotEmpty() && miUid != null && miNombreUsuario != null) {

            val mensaje = MensajeChat(
                mensaje = textoMensaje,
                nombre_usuario = miNombreUsuario!!,
                uid = miUid!!
            )

            chatRef.push().setValue(mensaje)
                .addOnSuccessListener {
                    etMensaje.setText("") 
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al enviar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun escucharMensajes() {
        llMensajesContainer.removeAllViews() 

        chatListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val mensaje = snapshot.getValue(MensajeChat::class.java)
                    if (mensaje != null) {
                        agregarMensajeALaUI(mensaje)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Chat_vecinal, "Error al leer chat: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        chatRef.orderByChild("timestamp").addChildEventListener(chatListener!!)
    }


    private fun agregarMensajeALaUI(mensaje: MensajeChat) {
        val inflater = LayoutInflater.from(this)
        val esMio = mensaje.uid == miUid
        val vista: View
        val timestamp = (mensaje.timestamp as? Long) ?: System.currentTimeMillis()
        val horaFormateada = formatTimestamp(timestamp) 
        if (esMio) {
            vista = inflater.inflate(R.layout.item_chat_propio, llMensajesContainer, false)
            val tvMensaje: TextView = vista.findViewById(R.id.tvMensajePropio)
            val tvHora: TextView = vista.findViewById(R.id.tvHoraPropio) 

            tvMensaje.text = mensaje.mensaje
            tvHora.text = horaFormateada 
        } else {
            vista = inflater.inflate(R.layout.item_chat_otro, llMensajesContainer, false)
            val tvNombre: TextView = vista.findViewById(R.id.tvNombreUsuarioOtro)
            val tvMensaje: TextView = vista.findViewById(R.id.tvMensajeOtro)
            val tvHora: TextView = vista.findViewById(R.id.tvHoraOtro) 

            tvNombre.text = mensaje.nombre_usuario
            tvMensaje.text = mensaje.mensaje
            tvHora.text = horaFormateada 
        }

        llMensajesContainer.addView(vista)

        svMensajes.post {
            svMensajes.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun setupStatusBar() {
        window.statusBarColor = Color.parseColor("#F5F5F5") 
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }
    }

    private fun setupDrawerMenuButton() {
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
            if (targetActivity != Chat_vecinal::class.java) {
                startActivity(Intent(this, targetActivity))
            }
        }

        llInicio.setOnClickListener { navigateAndHighlight(Home::class.java, R.id.llInicio) }
        llReportes.setOnClickListener { navigateAndHighlight(Reportes::class.java, R.id.llReportesMenu) }
        llAccesos.setOnClickListener { navigateAndHighlight(Acceso::class.java, R.id.llAccesosMenu) }
        llChat.setOnClickListener { highlightActiveMenuItem(R.id.llChatMenu); drawerLayout.closeDrawer(GravityCompat.START) }
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (chatListener != null) {
            chatRef.removeEventListener(chatListener!!)
        }
    }
}
