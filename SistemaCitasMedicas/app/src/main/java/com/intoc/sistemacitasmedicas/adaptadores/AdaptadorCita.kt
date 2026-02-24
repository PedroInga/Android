package com.intoc.sistemacitasmedicas.adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intoc.sistemacitasmedicas.R
import com.intoc.sistemacitasmedicas.bean.Cita
import com.intoc.sistemacitasmedicas.databinding.ItemCitaBinding

// Adaptador para mostrar la lista de citas en un RecyclerView
// Usa View Binding en el ViewHolder
// Recibe la lista y tres lambdas: ver detalle, editar y eliminar
class AdaptadorCita(
    private var listaCitas: ArrayList<Cita>,
    private val onVer: (Cita) -> Unit,           // Evento ver detalle
    private val onEditar: (Cita) -> Unit,         // Evento editar
    private val onEliminar: (Cita) -> Unit        // Evento eliminar
) : RecyclerView.Adapter<AdaptadorCita.CitaViewHolder>() {

    // ViewHolder con View Binding
    class CitaViewHolder(val binding: ItemCitaBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Infla el layout item_cita.xml usando View Binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val binding = ItemCitaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CitaViewHolder(binding)
    }

    // Asigna los datos de la cita a las vistas usando binding
    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]

        // Mostrar nombre del paciente
        holder.binding.tvPacienteCita.text = cita.nombrePaciente
        // Mostrar nombre del doctor con prefijo "Dr."
        holder.binding.tvDoctorCita.text = "Dr. ${cita.nombreDoctor}"
        // Mostrar fecha y hora
        holder.binding.tvFechaCita.text = cita.fecha
        holder.binding.tvHoraCita.text = cita.hora

        // Configurar el badge de estado con color según el estado
        holder.binding.tvEstadoCita.text = cita.estado
        when (cita.estado) {
            "Pendiente" -> holder.binding.tvEstadoCita.setBackgroundResource(R.drawable.bg_estado_pendiente)
            "Confirmada" -> holder.binding.tvEstadoCita.setBackgroundResource(R.drawable.bg_estado_confirmada)
            "Completada" -> holder.binding.tvEstadoCita.setBackgroundResource(R.drawable.bg_estado_completada)
            "Cancelada" -> holder.binding.tvEstadoCita.setBackgroundResource(R.drawable.bg_estado_cancelada)
            else -> holder.binding.tvEstadoCita.setBackgroundResource(R.drawable.bg_estado_pendiente)
        }

        // Evento botón ver detalle
        holder.binding.btnVerCita.setOnClickListener { onVer(cita) }

        // Evento botón editar
        holder.binding.btnEditarCita.setOnClickListener { onEditar(cita) }

        // Evento botón eliminar
        holder.binding.btnEliminarCita.setOnClickListener { onEliminar(cita) }
    }

    // Retorna cantidad de citas
    override fun getItemCount(): Int = listaCitas.size

    // Actualizar lista (búsqueda y refresco)
    fun actualizarLista(nuevaLista: ArrayList<Cita>) {
        listaCitas = nuevaLista
        notifyDataSetChanged()
    }
}