package org.utl.reddeseguridadvecinal.controller

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import org.utl.reddeseguridadvecinal.R
import org.utl.reddeseguridadvecinal.modelo.CargoMantenimientoDTO
import org.utl.reddeseguridadvecinal.modelo.CargoServicioDTO
import java.text.NumberFormat
import java.util.Locale

class SeleccionCargosDialogFragment : DialogFragment() {

    private lateinit var llListaCargos: LinearLayout
    private lateinit var tvContadorSeleccionados: TextView
    private lateinit var tvSaldoMaximo: TextView
    private lateinit var etMontoAbonar: EditText
    private lateinit var etCVV: EditText
    private lateinit var btnConfirmar: CardView
    private lateinit var btnCancelar: CardView
    private lateinit var btnSeleccionarTodos: CardView
    private lateinit var btnDeseleccionarTodos: CardView

    private var montoMaximo: Double = 0.0
    private var tipo: String = ""
    private var cargosMantenimiento: List<CargoMantenimientoDTO> = emptyList()
    private var cargosServicios: List<CargoServicioDTO> = emptyList()

    private var onCargosSeleccionadosListener: OnCargosSeleccionadosListener? = null

    interface OnCargosSeleccionadosListener {
        fun onCargosSeleccionados(
            cargosMantenimiento: List<CargoMantenimientoDTO>,
            cargosServicios: List<CargoServicioDTO>,
            montoTotal: Double,
            cvv: String
        )
    }

    companion object {
        fun newInstance(
            tipo: String,
            cargosMantenimiento: List<CargoMantenimientoDTO>,
            cargosServicios: List<CargoServicioDTO>,
            listener: OnCargosSeleccionadosListener
        ): SeleccionCargosDialogFragment {
            val fragment = SeleccionCargosDialogFragment()
            fragment.tipo = tipo
            fragment.cargosMantenimiento = cargosMantenimiento
            fragment.cargosServicios = cargosServicios
            fragment.onCargosSeleccionadosListener = listener
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.seleccion_cargos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        cargarCargos()
        setupListeners()
        actualizarResumen()
    }

    private fun setupViews(view: View) {
        llListaCargos = view.findViewById(R.id.llListaCargos)
        tvContadorSeleccionados = view.findViewById(R.id.tvContadorSeleccionados)
        tvSaldoMaximo = view.findViewById(R.id.tvSaldoMaximo)
        etMontoAbonar = view.findViewById(R.id.etMontoAbonar)
        etCVV = view.findViewById(R.id.etCVV)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        btnCancelar = view.findViewById(R.id.btnCancelar)
        btnSeleccionarTodos = view.findViewById(R.id.btnSeleccionarTodos)
        btnDeseleccionarTodos = view.findViewById(R.id.btnDeseleccionarTodos)

        // monto
        etMontoAbonar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validarCampos()
            }
        })

        // CVV
        etCVV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validarCampos()
            }
        })
    }

    private fun cargarCargos() {
        llListaCargos.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())

        // Cargos de mantenimiento
        cargosMantenimiento.forEach { cargo ->
            val itemView = inflater.inflate(R.layout.item_cargo_seleccion, llListaCargos, false)
            bindCargoView(itemView, cargo)
            llListaCargos.addView(itemView)
        }

        // Cargos de servicios
        cargosServicios.forEach { cargo ->
            val itemView = inflater.inflate(R.layout.item_cargo_seleccion, llListaCargos, false)
            bindCargoView(itemView, cargo)
            llListaCargos.addView(itemView)
        }
    }

    private fun bindCargoView(itemView: View, cargo: Any) {
        val cbSeleccion = itemView.findViewById<CheckBox>(R.id.cbSeleccionCargo)
        val tvDescripcion = itemView.findViewById<TextView>(R.id.tvDescripcionCargo)
        val tvPeriodo = itemView.findViewById<TextView>(R.id.tvPeriodoCargo)
        val tvMonto = itemView.findViewById<TextView>(R.id.tvMontoCargo)

        when (cargo) {
            is CargoMantenimientoDTO -> {
                tvDescripcion.text = cargo.concepto
                tvPeriodo.text = "Vence: ${formatearFecha(cargo.fechaVencimiento)}"
                tvMonto.text = "Saldo: ${formatearMoneda(cargo.saldoPendiente)}"
                cbSeleccion.tag = cargo
            }
            is CargoServicioDTO -> {
                tvDescripcion.text = cargo.concepto
                tvPeriodo.text = "Creado: ${formatearFecha(cargo.fechaCreacion)}"
                tvMonto.text = "Saldo: ${formatearMoneda(cargo.saldoPendiente)}"
                cbSeleccion.tag = cargo
            }
        }

        cbSeleccion.setOnCheckedChangeListener { _, isChecked ->
            actualizarResumen()
        }
    }

    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            confirmarSeleccion()
        }

        btnCancelar.setOnClickListener {
            dismiss()
        }

        btnSeleccionarTodos.setOnClickListener {
            seleccionarTodos(true)
        }

        btnDeseleccionarTodos.setOnClickListener {
            seleccionarTodos(false)
        }
    }

    private fun seleccionarTodos(seleccionar: Boolean) {
        for (i in 0 until llListaCargos.childCount) {
            val itemView = llListaCargos.getChildAt(i)
            val cbSeleccion = itemView.findViewById<CheckBox>(R.id.cbSeleccionCargo)
            cbSeleccion.isChecked = seleccionar
        }
        actualizarResumen()
    }

    private fun actualizarResumen() {
        val cargosSeleccionados = obtenerCargosSeleccionados()
        montoMaximo = calcularMontoMaximo()

        tvContadorSeleccionados.text = "${cargosSeleccionados.size} cargos seleccionados"
        tvSaldoMaximo.text = "Saldo máximo disponible: ${formatearMoneda(montoMaximo)}"

        // Validar todos los campos
        validarCampos()
    }

    private fun obtenerCargosSeleccionados(): List<Any> {
        val seleccionados = mutableListOf<Any>()
        for (i in 0 until llListaCargos.childCount) {
            val itemView = llListaCargos.getChildAt(i)
            val cbSeleccion = itemView.findViewById<CheckBox>(R.id.cbSeleccionCargo)
            if (cbSeleccion.isChecked) {
                seleccionados.add(cbSeleccion.tag!!)
            }
        }
        return seleccionados
    }

    private fun calcularMontoMaximo(): Double {
        var total = 0.0
        for (i in 0 until llListaCargos.childCount) {
            val itemView = llListaCargos.getChildAt(i)
            val cbSeleccion = itemView.findViewById<CheckBox>(R.id.cbSeleccionCargo)
            if (cbSeleccion.isChecked) {
                when (val cargo = cbSeleccion.tag) {
                    is CargoMantenimientoDTO -> total += cargo.saldoPendiente
                    is CargoServicioDTO -> total += cargo.saldoPendiente
                }
            }
        }
        return total
    }

    private fun validarCampos() {
        val montoIngresado = etMontoAbonar.text.toString().toDoubleOrNull() ?: 0.0
        val cvvIngresado = etCVV.text.toString().trim()

        // Validar monto
        val montoValido = montoIngresado in 0.01..montoMaximo

        // Validar CVV
        val cvvValido = validarCVV(cvvIngresado)

        // Habilitar boton si estan bien los 2
        if (montoValido && cvvValido) {
            habilitarBotonConfirmar()
        } else {
            deshabilitarBotonConfirmar()
        }
    }

    private fun validarCVV(cvv: String): Boolean {
        return cvv.length == 3 && cvv.all { it.isDigit() }
    }

    private fun habilitarBotonConfirmar() {
        btnConfirmar.isEnabled = true
        btnConfirmar.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
    }

    private fun deshabilitarBotonConfirmar() {
        btnConfirmar.isEnabled = false
        btnConfirmar.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.disabled_color))
    }

    private fun confirmarSeleccion() {
        val montoIngresado = etMontoAbonar.text.toString().toDoubleOrNull() ?: 0.0
        val cvvIngresado = etCVV.text.toString().trim()

        // Validaciones del monto
        if (montoIngresado <= 0 || montoIngresado > montoMaximo) {
            Toast.makeText(requireContext(), "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validarCVV(cvvIngresado)) {
            Toast.makeText(requireContext(), "Ingrese un CVV válido de 3 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        // Distribuir el monto entre los cargos seleccionados
        val (cargosMantenimientoDistribuidos, cargosServiciosDistribuidos) = distribuirMonto(montoIngresado)

        onCargosSeleccionadosListener?.onCargosSeleccionados(
            cargosMantenimientoDistribuidos,
            cargosServiciosDistribuidos,
            montoIngresado,
            cvvIngresado
        )

        dismiss()
    }

    private fun distribuirMonto(montoTotal: Double): Pair<List<CargoMantenimientoDTO>, List<CargoServicioDTO>> {
        val cargosMantenimientoDistribuidos = mutableListOf<CargoMantenimientoDTO>()
        val cargosServiciosDistribuidos = mutableListOf<CargoServicioDTO>()

        var montoRestante = montoTotal

        //cargos seleccionados en orden
        val cargosSeleccionados = mutableListOf<Pair<Any, Double>>()

        for (i in 0 until llListaCargos.childCount) {
            val itemView = llListaCargos.getChildAt(i)
            val cbSeleccion = itemView.findViewById<CheckBox>(R.id.cbSeleccionCargo)

            if (cbSeleccion.isChecked) {
                when (val cargo = cbSeleccion.tag) {
                    is CargoMantenimientoDTO -> cargosSeleccionados.add(Pair(cargo, cargo.saldoPendiente))
                    is CargoServicioDTO -> cargosSeleccionados.add(Pair(cargo, cargo.saldoPendiente))
                }
            }
        }

        // Distribuir el monto en orden
        for ((cargoOriginal, saldoPendiente) in cargosSeleccionados) {
            if (montoRestante <= 0) break

            val montoAAbonar = minOf(saldoPendiente, montoRestante)

            when (cargoOriginal) {
                is CargoMantenimientoDTO -> {
                    // Instancia con monto específico a abonar
                    val cargoDistribuido = cargoOriginal.copy(
                        saldoPendiente = montoAAbonar
                    )
                    cargosMantenimientoDistribuidos.add(cargoDistribuido)
                    montoRestante -= montoAAbonar
                }
                is CargoServicioDTO -> {
                    // Instancia con monto específico a abonar
                    val cargoDistribuido = cargoOriginal.copy(
                        saldoPendiente = montoAAbonar
                    )
                    cargosServiciosDistribuidos.add(cargoDistribuido)
                    montoRestante -= montoAAbonar
                }
            }
        }

        return Pair(cargosMantenimientoDistribuidos, cargosServiciosDistribuidos)
    }

    private fun formatearMoneda(monto: Double): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return formato.format(monto)
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            // Formato para la fecha
            fecha.substring(0, 10) // Toma solo YYYY-MM-DD
        } catch (e: Exception) {
            fecha
        }
    }
}