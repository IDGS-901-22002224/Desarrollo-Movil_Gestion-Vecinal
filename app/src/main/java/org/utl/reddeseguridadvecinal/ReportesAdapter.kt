package org.utl.reddeseguridadvecinal

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.utl.reddeseguridadvecinal.modelo.ReporteResponse
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ReportesAdapter(private var reportes: List<ReporteResponse>) :
    RecyclerView.Adapter<ReportesAdapter.ReporteViewHolder>() {

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val outputFormat = SimpleDateFormat("dd/MM/yyyy - h:mm a", Locale.getDefault())

    class ReporteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vEstadoColor: View = itemView.findViewById(R.id.vEstadoColor)
        val ivEstadoIcono: ImageView = itemView.findViewById(R.id.ivEstadoIcono)
        val tvNombreUsuario: TextView = itemView.findViewById(R.id.tvNombreUsuario)
        val tvReporteUbicacion: TextView = itemView.findViewById(R.id.tvReporteUbicacion)
        val tvReporteFecha: TextView = itemView.findViewById(R.id.tvReporteFecha)
        val tvReporteDescripcion: TextView = itemView.findViewById(R.id.tvReporteDescripcion)
        val cvReporteTipoContainer: CardView = itemView.findViewById(R.id.cvReporteTipoContainer)
        val tvReporteTipoLabel: TextView = itemView.findViewById(R.id.tvReporteTipoLabel)
        val ivReporteImagen: ImageView = itemView.findViewById(R.id.ivReporteImagen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reporte, parent, false)
        return ReporteViewHolder(view)
    }

    override fun getItemCount() = reportes.size

    override fun onBindViewHolder(holder: ReporteViewHolder, position: Int) {
        val reporte = reportes[position]

        // 1. Textos básicos
        holder.tvNombreUsuario.text = reporte.nombreUsuario
        holder.tvReporteUbicacion.text = reporte.direccionTexto.ifEmpty { "Sin ubicación" }
        holder.tvReporteDescripcion.text = reporte.descripcion
        holder.tvReporteFecha.text = formatarFecha(reporte.fechaCreacion)

        if (reporte.visto) {
            val colorVisto = Color.parseColor("#FBBF24")
            holder.vEstadoColor.setBackgroundColor(colorVisto)
            holder.ivEstadoIcono.setImageResource(R.drawable.ic_visibility)
            holder.ivEstadoIcono.setColorFilter(colorVisto)
        } else {
            val colorNoVisto = Color.parseColor("#EF4444")
            holder.vEstadoColor.setBackgroundColor(colorNoVisto)
            holder.ivEstadoIcono.setImageResource(R.drawable.ic_visibility_off)
            holder.ivEstadoIcono.setColorFilter(colorNoVisto)
        }

        val (colorFondo, colorTexto) = getColorForTipo(reporte.tipoReporteID)

        holder.tvReporteTipoLabel.text = getNombreTipoPorId(reporte.tipoReporteID)

        holder.cvReporteTipoContainer.setCardBackgroundColor(Color.parseColor(colorFondo))
        holder.tvReporteTipoLabel.setTextColor(Color.parseColor(colorTexto))

        if (!reporte.imagenBase64.isNullOrEmpty()) {
            try {
                val base64Image = reporte.imagenBase64.split(",").getOrNull(1) ?: reporte.imagenBase64
                val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.ivReporteImagen.setImageBitmap(bitmap)
                holder.ivReporteImagen.visibility = View.VISIBLE
            } catch (e: Exception) {
                holder.ivReporteImagen.visibility = View.GONE
            }
        } else {
            holder.ivReporteImagen.visibility = View.GONE
        }
    }


    private fun getNombreTipoPorId(tipoId: Int): String {
        return when (tipoId) {
            1 -> "Robo"
            2 -> "Vandalismo"
            3 -> "Act. Sospechosa"
            4 -> "Falla Servicio"
            5 -> "Ruido Excesivo"
            6 -> "Bache/Obstaculo"
            else -> "Otro"
        }
    }

    private fun getColorForTipo(tipoId: Int): Pair<String, String> {
        return when (tipoId) {
            1 -> Pair("#FEE2E2", "#991B1B") // Rojo
            2 -> Pair("#FEF3C7", "#92400E") // Ambar
            3 -> Pair("#FFEDD5", "#9A3412") // Naranja
            4 -> Pair("#DBEAFE", "#1E40AF") // Azul
            5 -> Pair("#E0E7FF", "#3730A3") // Indigo
            6 -> Pair("#D1FAE5", "#065F46") // Verde
            else -> Pair("#E5E7EB", "#374151") // Gris
        }
    }

    fun actualizarLista(nuevaLista: List<ReporteResponse>) {
        reportes = nuevaLista
        notifyDataSetChanged()
    }

    private fun formatarFecha(fechaString: String): String {
        return try {
            val fecha = inputFormat.parse(fechaString.split(".")[0])
            outputFormat.format(fecha!!)
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }
}