package com.intoc.sistemacitasmedicas.adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intoc.sistemacitasmedicas.bean.Doctor
import com.intoc.sistemacitasmedicas.databinding.ItemDoctorBinding

// Adaptador para mostrar la lista de doctores en un RecyclerView
// Usa View Binding en el ViewHolder para acceder a las vistas sin findViewById
// Recibe la lista y dos lambdas para editar y eliminar
class AdaptadorDoctor(
    private var listaDoctores: ArrayList<Doctor>,          // Lista de datos
    private val onEditar: (Doctor) -> Unit,                // Evento editar
    private val onEliminar: (Doctor) -> Unit               // Evento eliminar
) : RecyclerView.Adapter<AdaptadorDoctor.DoctorViewHolder>() {

    // ViewHolder con View Binding: recibe el binding del item
    class DoctorViewHolder(val binding: ItemDoctorBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Infla el layout item_doctor.xml usando View Binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DoctorViewHolder(binding)
    }

    // Asigna los datos del doctor a las vistas usando binding
    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = listaDoctores[position]

        // Mostrar nombre completo con prefijo "Dr."
        holder.binding.tvNombreDoctor.text = "Dr. ${doctor.nombres}"
        // Mostrar especialidad
        holder.binding.tvEspecialidadDoctor.text = doctor.especialidad
        // Mostrar número de colegiatura
        holder.binding.tvCmpDoctor.text = "CMP: ${doctor.colegiatura}"

        // Evento botón editar
        holder.binding.btnEditarDoctor.setOnClickListener {
            onEditar(doctor)
        }

        // Evento botón eliminar
        holder.binding.btnEliminarDoctor.setOnClickListener {
            onEliminar(doctor)
        }
    }

    // Retorna la cantidad de doctores en la lista
    override fun getItemCount(): Int = listaDoctores.size

    // Método para actualizar la lista (búsqueda y refresco)
    fun actualizarLista(nuevaLista: ArrayList<Doctor>) {
        listaDoctores = nuevaLista
        notifyDataSetChanged()
    }
}