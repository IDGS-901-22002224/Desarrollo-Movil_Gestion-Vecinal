package org.utl.reddeseguridadvecinal.controller

import org.utl.reddeseguridadvecinal.api.Api
import org.utl.reddeseguridadvecinal.modelo.*
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*
import java.text.SimpleDateFormat

interface PagosApiService {
    @GET("api/Pagos/cuenta/{usuarioId}")
    suspend fun obtenerCuentaUsuario(@Path("usuarioId") usuarioId: Int): CuentaUsuarioResponse
}

class PagosServiciosController {
    private val apiService = Api.createService(PagosApiService::class.java)

    suspend fun obtenerDatosCuenta(usuarioId: Int): CuentaUsuarioResponse? {
        return try {
            println("Obteniendo datos de cuenta para usuario: $usuarioId")
            val response = apiService.obtenerCuentaUsuario(usuarioId)
            println("Datos de cuenta obtenidos exitosamente")
            response
        } catch (e: Exception) {
            println("Error al obtener datos de cuenta: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun filtrarDatosMesActual(cuenta: CuentaUsuarioResponse): DatosMesActual {
        val calendar = Calendar.getInstance()
        val mesActual = calendar.get(Calendar.MONTH) + 1
        val añoActual = calendar.get(Calendar.YEAR)

        println("Filtrando datos para mes: $mesActual, año: $añoActual")

        // Filtrar cargos de mantenimiento del mes actual
        val mantenimientoMes = cuenta.cargosMantenimiento.filter { cargo ->
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaVencimiento = sdf.parse(cargo.fechaVencimiento)
                val cal = Calendar.getInstance()
                cal.time = fechaVencimiento

                cal.get(Calendar.MONTH) + 1 == mesActual && cal.get(Calendar.YEAR) == añoActual
            } catch (e: Exception) {
                false
            }
        }

        // Filtrar cargos de servicios del mes actual
        val serviciosMes = cuenta.cargosServicios.filter { cargo ->
            try {
                val fechaStr = cargo.fechaCreacion.substring(0, 10)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaCreacion = sdf.parse(fechaStr)
                val cal = Calendar.getInstance()
                cal.time = fechaCreacion

                cal.get(Calendar.MONTH) + 1 == mesActual && cal.get(Calendar.YEAR) == añoActual
            } catch (e: Exception) {
                false
            }
        }

        println("Cargos mantenimiento del mes: ${mantenimientoMes.size}")
        println("Cargos servicios del mes: ${serviciosMes.size}")

        // Calcular totales
        val mantenimientoPagado = mantenimientoMes.sumOf { it.montoPagado }
        val mantenimientoPendiente = mantenimientoMes.sumOf { it.saldoPendiente }
        val serviciosPagado = serviciosMes.sumOf { it.montoPagado }
        val serviciosPendiente = serviciosMes.sumOf { it.saldoPendiente }

        val totalMantenimiento = mantenimientoPagado + mantenimientoPendiente
        val totalServicios = serviciosPagado + serviciosPendiente

        println("Mantenimiento - Pagado: $mantenimientoPagado, Pendiente: $mantenimientoPendiente, Total: $totalMantenimiento")
        println("Servicios - Pagado: $serviciosPagado, Pendiente: $serviciosPendiente, Total: $totalServicios")

        return DatosMesActual(
            mantenimientoPagado = mantenimientoPagado,
            mantenimientoPendiente = mantenimientoPendiente,
            serviciosPagado = serviciosPagado,
            serviciosPendiente = serviciosPendiente,
            totalMantenimiento = totalMantenimiento,
            totalServicios = totalServicios
        )
    }

    fun calcularDatosGrafica(datosMes: DatosMesActual): DatosGraficaPagos {
        val totalMantenimiento = datosMes.totalMantenimiento
        val totalServicios = datosMes.totalServicios
        val totalGeneral = totalMantenimiento + totalServicios

        println("Calculando gráfica para el mes actual:")
        println("Total Mantenimiento: $totalMantenimiento")
        println("Total Servicios: $totalServicios")
        println("Total General: $totalGeneral")

        // Calcular porcentajes
        val porcentajeMantenimiento = if (totalGeneral > 0) {
            (totalMantenimiento / totalGeneral * 100).toFloat()
        } else {
            50f
        }

        val porcentajeServicios = if (totalGeneral > 0) {
            (totalServicios / totalGeneral * 100).toFloat()
        } else {
            50f
        }

        println("Porcentaje Mantenimiento: $porcentajeMantenimiento%")
        println("Porcentaje Servicios: $porcentajeServicios%")

        // Calcular ángulos para la gráfica
        val anguloMantenimiento = (porcentajeMantenimiento / 100) * 360
        val anguloServicios = (porcentajeServicios / 100) * 360

        return DatosGraficaPagos(
            totalMantenimiento = totalMantenimiento,
            totalServicios = totalServicios,
            porcentajeMantenimiento = porcentajeMantenimiento,
            porcentajeServicios = porcentajeServicios,
            anguloMantenimiento = anguloMantenimiento,
            anguloServicios = anguloServicios
        )
    }

    suspend fun obtenerDatosParaGrafica(usuarioId: Int): DatosGraficaPagos? {
        val cuenta = obtenerDatosCuenta(usuarioId)
        return cuenta?.let {
            val datosMes = filtrarDatosMesActual(it)
            calcularDatosGrafica(datosMes)
        }
    }

    // Método para obtener datos detallados del mes
    suspend fun obtenerDatosMesActual(usuarioId: Int): DatosMesActual? {
        val cuenta = obtenerDatosCuenta(usuarioId)
        return cuenta?.let { filtrarDatosMesActual(it) }
    }
}