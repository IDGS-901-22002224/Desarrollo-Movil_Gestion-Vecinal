package org.utl.reddeseguridadvecinal.logica

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import org.utl.reddeseguridadvecinal.controller.PagosController
import org.utl.reddeseguridadvecinal.modelo.*
import org.utl.reddeseguridadvecinal.util.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class PagarLogica {
    private val pagosController = PagosController()

    fun cerrarSesion(context: Context) {
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
    }

    suspend fun obtenerDatosCuenta(usuarioId: Int): CuentaUsuarioResponse? {
        return pagosController.obtenerCuentaUsuario(usuarioId)
    }

    suspend fun realizarPagoMantenimiento(
        usuarioId: Int,
        cargosMantenimiento: List<CargoMantenimientoDTO>
    ): ByteArray? {
        return pagosController.pagarMantenimientoCompleto(usuarioId, cargosMantenimiento)
    }

    suspend fun realizarPagoServicios(
        usuarioId: Int,
        cargosServicios: List<CargoServicioDTO>
    ): ByteArray? {
        return pagosController.pagarServiciosCompletos(usuarioId, cargosServicios)
    }

    fun calcularTotalesMantenimiento(cargos: List<CargoMantenimientoDTO>): Triple<Double, Double, Double> {
        val esteMes = cargos.filter {
            esDelMesActual(it.fechaVencimiento)
        }.sumOf { it.saldoPendiente }

        val adeudos = cargos.filterNot {
            esDelMesActual(it.fechaVencimiento)
        }.sumOf { it.saldoPendiente }

        val total = esteMes + adeudos
        return Triple(esteMes, adeudos, total)
    }

    fun calcularTotalesServicios(cargos: List<CargoServicioDTO>): Triple<Double, Double, Double> {
        val esteMes = cargos.filter {
            esDelMesActual(it.fechaCreacion)
        }.sumOf { it.saldoPendiente }

        val adeudos = cargos.filterNot {
            esDelMesActual(it.fechaCreacion)
        }.sumOf { it.saldoPendiente }

        val total = esteMes + adeudos
        return Triple(esteMes, adeudos, total)
    }

    private fun esDelMesActual(fechaString: String): Boolean {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = formato.parse(fechaString)
            val ahora = Calendar.getInstance()
            val fechaCargo = Calendar.getInstance().apply { time = fecha }

            fechaCargo.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) &&
                    fechaCargo.get(Calendar.MONTH) == ahora.get(Calendar.MONTH)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun realizarAbonoParcial(
        usuarioId: Int,
        cargosMantenimiento: List<CargoMantenimientoDTO>,
        cargosServicios: List<CargoServicioDTO>,
        montoTotal: Double,
        cvv: String
    ): ByteArray? {

        return try {
            val abonoRequest = AbonoParcialRequest(
                usuarioId = usuarioId,
                cargosMantenimiento = cargosMantenimiento.map { cargo ->
                    CargoAbonoDTO(
                        cargoId = cargo.cargoMantenimientoID,
                        montoAbonar = cargo.saldoPendiente,
                        tipo = "mantenimiento"
                    )
                },
                cargosServicios = cargosServicios.map { cargo ->
                    CargoAbonoDTO(
                        cargoId = cargo.cargoServicioID,
                        montoAbonar = cargo.saldoPendiente,
                        tipo = "servicio"
                    )
                },
                montoTotal = montoTotal,
                fechaAbono = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )

            pagosController.realizarAbonoParcial(abonoRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}