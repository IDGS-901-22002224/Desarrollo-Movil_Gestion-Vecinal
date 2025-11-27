package org.utl.reddeseguridadvecinal

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.utl.reddeseguridadvecinal.modelo.InvitadoResponse
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class InvitadosAdapter(private var invitados: List<InvitadoResponse>) :
    RecyclerView.Adapter<InvitadosAdapter.InvitadoViewHolder>() {

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val outputTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    class InvitadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vEstadoColor: View = itemView.findViewById(R.id.vEstadoColor)
        val ivIcono: ImageView = itemView.findViewById(R.id.ivIcono)
        val tvFechaVisita: TextView = itemView.findViewById(R.id.tvFechaVisita)
        val tvNombreInvitado: TextView = itemView.findViewById(R.id.tvNombreInvitado)
        val tvEstadoHorarios: TextView = itemView.findViewById(R.id.tvEstadoHorarios)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitadoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invitado, parent, false)
        return InvitadoViewHolder(view)
    }

    override fun getItemCount() = invitados.size

    override fun onBindViewHolder(holder: InvitadoViewHolder, position: Int) {
        val invitado = invitados[position]

        // Nombre
        holder.tvNombreInvitado.text = "${invitado.nombreInvitado} ${invitado.apellidoPaternoInvitado}"

        val fechaVisitaStr = formatearFecha(invitado.fechaGeneracion)
        holder.tvFechaVisita.text = fechaVisitaStr

        val colorEstado: Int
        val textoEstado: String

        when (invitado.estado) {

            "Expirado" -> {
                colorEstado = Color.parseColor("#EF4444") // Rojo
                textoEstado = "Expirado"
            }

            "Dentro" -> {
                colorEstado = Color.parseColor("#10B981") // Verde
                val horaEntrada = formatearHora(invitado.fechaEntrada)
                textoEstado = "Entrada: $horaEntrada  |  Salida: --:--"
            }

            "Completado" -> {
                colorEstado = Color.parseColor("#10B981") // Verde
                val horaEntrada = formatearHora(invitado.fechaEntrada)
                val horaSalida = formatearHora(invitado.fechaSalida)
                textoEstado = "Entrada: $horaEntrada  |  Salida: $horaSalida"
            }

            else -> { // "Pendiente"
                colorEstado = Color.parseColor("#FBBF24") // Amarillo
                textoEstado = "Pendiente de ingreso"
            }
        }


        holder.vEstadoColor.setBackgroundColor(colorEstado)
        holder.ivIcono.setColorFilter(colorEstado)
        holder.tvEstadoHorarios.text = textoEstado
    }

    fun actualizarLista(nuevaLista: List<InvitadoResponse>) {
        invitados = nuevaLista
        notifyDataSetChanged()
    }

    private fun formatearFecha(fechaString: String?): String {
        if (fechaString == null) return "--/--/----"
        return try {
            val fecha = inputFormat.parse(fechaString.split(".")[0])
            outputDateFormat.format(fecha!!)
        } catch (e: Exception) {
            fechaString.take(10)
        }
    }

    private fun formatearHora(fechaString: String?): String {
        if (fechaString == null) return "--:--"
        return try {
            val fecha = inputFormat.parse(fechaString.split(".")[0])
            outputTimeFormat.format(fecha!!)
        } catch (e: Exception) {
            "--:--"
        }
    }
}