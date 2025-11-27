
package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.*
import retrofit2.http.*
import okhttp3.ResponseBody
import retrofit2.Response

interface PagarApiService {
    @GET("api/Pagos/cuenta/{usuarioId}")
    suspend fun obtenerCuentaUsuario(@Path("usuarioId") usuarioId: Int): CuentaUsuarioResponse

    @POST("api/Pagos")
    @Streaming
    suspend fun registrarPago(@Body request: PagoRegistroRequest): okhttp3.ResponseBody
}

class PagosController {
    private val apiService = Api.createService(PagarApiService::class.java)

    suspend fun obtenerCuentaUsuario(usuarioId: Int): CuentaUsuarioResponse? {
        return try {
            //println("cuenta del usuario: $usuarioId")
            val response = apiService.obtenerCuentaUsuario(usuarioId)
            response
        } catch (e: Exception) {
            //println("Error al obtener cuenta del usuario: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun registrarPagoCompleto(
        usuarioId: Int,
        montoTotal: Double,
        tipoPago: String,
        metodoPago: String,
        detalles: List<DetallePagoRequest>
    ): ByteArray? {
        return try {
            /*
            println(" pago para usuario: $usuarioId")
            println("Monto total: $montoTotal")
            println("Tipo pago: $tipoPago")
            println("Metodo pago: $metodoPago")
            println("Numero de detalles: ${detalles.size}")
            */

            // JSON
            val detallesJson = construirJsonManual(detalles)

            val pagoRequest = PagoRegistroRequest(
                usuarioID = usuarioId,
                montoTotal = montoTotal,
                tipoPago = tipoPago,
                metodoPago = metodoPago,
                detallesPagoJson = detallesJson
            )

            //println("PagoRequest completo: $pagoRequest")

            val response = apiService.registrarPago(pagoRequest)
            val pdfBytes = response.bytes()
            //println("Pago registrado exitosamente, PDF generado: ${pdfBytes.size} bytes")
            pdfBytes
        } catch (e: Exception) {
            //println("Error al registrar pago: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun construirJsonManual(detalles: List<DetallePagoRequest>): String {
        val detallesArray = detalles.joinToString(",") { detalle ->
            """
        {
            "CargoMantenimientoID": ${detalle.cargoMantenimientoID ?: "null"},
            "CargoServicioID": ${detalle.cargoServicioID ?: "null"},
            "MontoAplicado": ${detalle.montoAplicado}
        }
        """.trimIndent()
        }
        val jsonResult = "[$detallesArray]"

        //println("JSON enviado al backend: $jsonResult")

        return jsonResult
    }

    // pago completo de mantenimiento
    suspend fun pagarMantenimientoCompleto(
        usuarioId: Int,
        cargosMantenimiento: List<CargoMantenimientoDTO>
    ): ByteArray? {
        val detalles = cargosMantenimiento.map { cargo ->
            DetallePagoRequest(
                montoAplicado = cargo.saldoPendiente,
                cargoMantenimientoID = cargo.cargoMantenimientoID,
                cargoServicioID = null
            )
        }

        val montoTotal = cargosMantenimiento.sumOf { it.saldoPendiente }

        return registrarPagoCompleto(
            usuarioId = usuarioId,
            montoTotal = montoTotal,
            tipoPago = "Mantenimiento",
            metodoPago = "Tarjeta",
            detalles = detalles
        )
    }

    // pago completo de servicios
    suspend fun pagarServiciosCompletos(
        usuarioId: Int,
        cargosServicios: List<CargoServicioDTO>
    ): ByteArray? {
        val detalles = cargosServicios.map { cargo ->
            DetallePagoRequest(
                montoAplicado = cargo.saldoPendiente,
                cargoMantenimientoID = null,
                cargoServicioID = cargo.cargoServicioID
            )
        }

        val montoTotal = cargosServicios.sumOf { it.saldoPendiente }

        return registrarPagoCompleto(
            usuarioId = usuarioId,
            montoTotal = montoTotal,
            tipoPago = "Servicios",
            metodoPago = "Tarjeta",
            detalles = detalles
        )
    }

    suspend fun realizarAbonoParcial(abonoRequest: AbonoParcialRequest): ByteArray? {
        return try {

            val detalles = mutableListOf<DetallePagoRequest>()

            abonoRequest.cargosMantenimiento.forEach { cargo ->
                detalles.add(DetallePagoRequest(
                    montoAplicado = cargo.montoAbonar,
                    cargoMantenimientoID = cargo.cargoId,
                    cargoServicioID = null
                ))
            }

            abonoRequest.cargosServicios.forEach { cargo ->
                detalles.add(DetallePagoRequest(
                    montoAplicado = cargo.montoAbonar,
                    cargoMantenimientoID = null,
                    cargoServicioID = cargo.cargoId
                ))
            }

            // mismo metodo de pago completo pero con tipo Abono Parcial
            registrarPagoCompleto(
                usuarioId = abonoRequest.usuarioId,
                montoTotal = abonoRequest.montoTotal,
                tipoPago = "Abono Parcial",
                metodoPago = "Tarjeta",
                detalles = detalles
            )
        } catch (e: Exception) {
            // println("Error en abono parcial: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}