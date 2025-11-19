package org.utl.reddeseguridadvecinal

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte, parent, false)
        return ReporteViewHolder(view)
    }


    override fun getItemCount() = reportes.size

    override fun onBindViewHolder(holder: ReporteViewHolder, position: Int) {
        val reporte = reportes[position]

        holder.tvNombreUsuario.text = reporte.nombreUsuario
        holder.tvReporteUbicacion.text = reporte.direccionTexto.ifEmpty { "Sin ubicación" }
        holder.tvReporteDescripcion.text = reporte.descripcion
        holder.tvReporteFecha.text = formatarFecha(reporte.fechaCreacion)


        if (reporte.visto) {
            val colorVisto = Color.parseColor("#FBBF24") // Amarillo
            holder.vEstadoColor.setBackgroundColor(colorVisto)
            holder.ivEstadoIcono.setImageResource(R.drawable.ic_visibility) // Icono de ojo abierto
            holder.ivEstadoIcono.setColorFilter(colorVisto)
        } else {
            val colorNoVisto = Color.parseColor("#EF4444") // Rojo
            holder.vEstadoColor.setBackgroundColor(colorNoVisto)
            holder.ivEstadoIcono.setImageResource(R.drawable.ic_visibility_off) // Icono de ojo cerrado
            holder.ivEstadoIcono.setColorFilter(colorNoVisto)
        }

        val (colorFondoEtiqueta, colorTextoEtiqueta) = getColorForTipo(reporte.tipoReporteID)
        val nombreTipoReporte = getNombreTipoPorId(reporte.tipoReporteID)

        holder.tvReporteTipoLabel.text = nombreTipoReporte
        holder.cvReporteTipoContainer.setCardBackgroundColor(Color.parseColor(colorFondoEtiqueta))
        holder.tvReporteTipoLabel.setTextColor(Color.parseColor(colorTextoEtiqueta))
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
            1 -> Pair("#FEE2E2", "#991B1B") // Robo
            2 -> Pair("#FEF3C7", "#92400E") // Vandalismo
            3 -> Pair("#FFEDD5", "#9A3412") // Act. Sospechosa
            4 -> Pair("#DBEAFE", "#1E40AF") // Falla Servicio
            5 -> Pair("#E0E7FF", "#3730A3") // Ruido
            6 -> Pair("#D1FAE5", "#065F46") // Bache
            else -> Pair("#E5E7EB", "#374151") // Otro
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