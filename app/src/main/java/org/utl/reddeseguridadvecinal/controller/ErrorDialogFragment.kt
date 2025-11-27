package org.utl.reddeseguridadvecinal.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import org.utl.reddeseguridadvecinal.R

class ErrorDialogFragment : DialogFragment() {

    private var titulo: String = "ERROR"
    private var mensajePrincipal: String = ""
    private var mensajeSecundario: String = ""
    private var textoBotonAceptar: String = "Volver"
    private var onAceptarListener: (() -> Unit)? = null

    companion object {
        fun newInstance(
            titulo: String = "ERROR",
            mensajePrincipal: String,
            mensajeSecundario: String = "",
            textoBotonAceptar: String = "Volver",
            onAceptar: (() -> Unit)? = null
        ): ErrorDialogFragment {
            return ErrorDialogFragment().apply {
                this.titulo = titulo
                this.mensajePrincipal = mensajePrincipal
                this.mensajeSecundario = mensajeSecundario
                this.textoBotonAceptar = textoBotonAceptar
                this.onAceptarListener = onAceptar
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        println("🔍 ErrorDialogFragment: Inflando layout modal_error")
        return inflater.inflate(R.layout.modal_error, container, false) // ← CAMBIAR AQUÍ
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("🔍 ErrorDialogFragment: onViewCreated iniciado")

        try {
            // Buscar las vistas
            val tvTituloError = view.findViewById<TextView>(R.id.tvTituloEmergencia)
            val tvMensajePrincipal = view.findViewById<TextView>(R.id.tvMensajePrincipal)
            val tvMensajeSecundario = view.findViewById<TextView>(R.id.tvMensajeSecundario)
            val btnAceptar = view.findViewById<CardView>(R.id.btnConfirmar)

            println("🔍 Vistas encontradas:")
            println("   - tvTituloEmergencia: ${tvTituloError != null}")
            println("   - tvMensajePrincipal: ${tvMensajePrincipal != null}")
            println("   - tvMensajeSecundario: ${tvMensajeSecundario != null}")
            println("   - btnConfirmar: ${btnAceptar != null}")

            // Configurar textos
            tvTituloError.text = titulo
            tvMensajePrincipal.text = mensajePrincipal

            if (mensajeSecundario.isNotEmpty()) {
                tvMensajeSecundario.text = mensajeSecundario
                tvMensajeSecundario.visibility = View.VISIBLE
            } else {
                tvMensajeSecundario.visibility = View.GONE
            }

            // Configurar texto del botón
            val textoBoton = btnAceptar.getChildAt(0) as TextView
            textoBoton.text = textoBotonAceptar

            // Configurar listener
            btnAceptar.setOnClickListener {
                println("🔍 Botón aceptar presionado")
                onAceptarListener?.invoke()
                dismiss()
            }

            println("✅ Modal de error configurado correctamente")

        } catch (e: Exception) {
            println("❌ Error en ErrorDialogFragment: ${e.message}")
            e.printStackTrace()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}