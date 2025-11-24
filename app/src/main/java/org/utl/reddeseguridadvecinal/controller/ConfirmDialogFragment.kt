
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

class ConfirmDialogFragment : DialogFragment() {

    private lateinit var tvTituloAlerta: TextView
    private lateinit var tvMensajePrincipal: TextView
    private lateinit var tvMensajeSecundario: TextView
    private lateinit var btnConfirmar: CardView
    private lateinit var btnCancelar: CardView

    private var titulo: String = "ALERTA"
    private var mensajePrincipal: String = ""
    private var mensajeSecundario: String = ""
    private var textoBotonConfirmar: String = "Confirmar"
    private var textoBotonCancelar: String = "Cancelar"
    private var onConfirmListener: (() -> Unit)? = null

    companion object {
        fun newInstance(
            titulo: String = "ALERTA",
            mensajePrincipal: String,
            mensajeSecundario: String = "",
            textoBotonConfirmar: String = "Confirmar",
            textoBotonCancelar: String = "Cancelar",
            onConfirm: (() -> Unit)? = null
        ): ConfirmDialogFragment {
            return ConfirmDialogFragment().apply {
                this.titulo = titulo
                this.mensajePrincipal = mensajePrincipal
                this.mensajeSecundario = mensajeSecundario
                this.textoBotonConfirmar = textoBotonConfirmar
                this.textoBotonCancelar = textoBotonCancelar
                this.onConfirmListener = onConfirm
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.modal_confirmar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enlazar vistas
        tvTituloAlerta = view.findViewById(R.id.tvTituloAlerta)
        tvMensajePrincipal = view.findViewById(R.id.tvMensajePrincipal)
        tvMensajeSecundario = view.findViewById(R.id.tvMensajeSecundario)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        btnCancelar = view.findViewById(R.id.btnCancelar)

        // Configurar textos
        setupTexts()
        setupListeners()
    }

    private fun setupTexts() {
        tvTituloAlerta.text = titulo
        tvMensajePrincipal.text = mensajePrincipal

        if (mensajeSecundario.isNotEmpty()) {
            tvMensajeSecundario.text = mensajeSecundario
            tvMensajeSecundario.visibility = View.VISIBLE
        } else {
            tvMensajeSecundario.visibility = View.GONE
        }

        // Configurar textos de botones
        (btnConfirmar.getChildAt(0) as? TextView)?.text = textoBotonConfirmar
        (btnCancelar.getChildAt(0) as? TextView)?.text = textoBotonCancelar
    }

    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss()
        }

        btnCancelar.setOnClickListener {
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