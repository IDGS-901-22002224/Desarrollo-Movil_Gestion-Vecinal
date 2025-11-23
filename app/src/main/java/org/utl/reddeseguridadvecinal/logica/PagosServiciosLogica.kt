package org.utl.reddeseguridadvecinal.logica

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import org.utl.reddeseguridadvecinal.modelo.DatosGraficaPagos
import org.utl.reddeseguridadvecinal.util.SessionManager

class PagosServiciosLogica {

    fun cerrarSesion(context: Context) {
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
    }

    fun crearGraficaPastel(datos: DatosGraficaPagos): Bitmap {
        val size = 400 // Tamaño del bitmap
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(50f, 50f, size - 50f, size - 50f)
        val radio = (size - 100) / 2f

        // Colores
        val colorMantenimiento = Color.parseColor("#047857") // Verde oscuro
        val colorServicios = Color.parseColor("#10B981")    // Verde claro

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Dibujar segmento de mantenimiento
        paint.color = colorMantenimiento
        canvas.drawArc(rect, -90f, datos.anguloMantenimiento, true, paint)

        // Dibujar segmento de servicios
        paint.color = colorServicios
        canvas.drawArc(rect, -90f + datos.anguloMantenimiento, datos.anguloServicios, true, paint)

        // agregar borde
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, radio, paint)

        return bitmap
    }

    fun formatearTextoVivienda(datos: DatosGraficaPagos, datosMes: org.utl.reddeseguridadvecinal.modelo.DatosMesActual? = null): String {
        return if (datosMes != null) {
            "Mantenimiento: $${"%.2f".format(datosMes.totalMantenimiento)} " +
                    "($${"%.2f".format(datosMes.mantenimientoPagado)} pagado, " +
                    "$${"%.2f".format(datosMes.mantenimientoPendiente)} pendiente) " +
                    "(${"%.1f".format(datos.porcentajeMantenimiento)}%)"
        } else {
            "Pagos de vivienda: $${"%.2f".format(datos.totalMantenimiento)} (${"%.1f".format(datos.porcentajeMantenimiento)}%)"
        }
    }

    fun formatearTextoServicios(datos: DatosGraficaPagos, datosMes: org.utl.reddeseguridadvecinal.modelo.DatosMesActual? = null): String {
        return if (datosMes != null) {
            "Servicios: $${"%.2f".format(datosMes.totalServicios)} " +
                    "($${"%.2f".format(datosMes.serviciosPagado)} pagado, " +
                    "$${"%.2f".format(datosMes.serviciosPendiente)} pendiente) " +
                    "(${"%.1f".format(datos.porcentajeServicios)}%)"
        } else {
            "Pagos de servicios: $${"%.2f".format(datos.totalServicios)} (${"%.1f".format(datos.porcentajeServicios)}%)"
        }
    }
}