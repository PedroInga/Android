package com.intoc.sistemacitasmedicas.adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intoc.sistemacitasmedicas.bean.Paciente
import com.intoc.sistemacitasmedicas.databinding.ItemPacienteBinding

// Adaptador para mostrar la lista de pacientes en un RecyclerView
// Usa View Binding en el ViewHolder para acceder a las vistas sin findViewById
// Recibe la lista de pacientes y dos lambdas para los eventos de editar y eliminar
class AdaptadorPaciente(
    private var listaPacientes: ArrayList<Paciente>,       // Lista de datos
    private val onEditar: (Paciente) -> Unit,              // Evento al presionar editar
    private val onEliminar: (Paciente) -> Unit             // Evento al presionar eliminar
) : RecyclerView.Adapter<AdaptadorPaciente.PacienteViewHolder>() {

    // ViewHolder con View Binding: mantiene la referencia al binding del item
    // En lugar de usar findViewById, accede a las vistas como binding.tvNombrePaciente
    class PacienteViewHolder(val binding: ItemPacienteBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Infla el layout item_paciente.xml usando View Binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacienteViewHolder {
        // Inflar con View Binding en lugar de LayoutInflater.inflate()
        val binding = ItemPacienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PacienteViewHolder(binding)
    }

    // Asigna los datos del paciente a las vistas del item usando binding
    override fun onBindViewHolder(holder: PacienteViewHolder, position: Int) {
        val paciente = listaPacientes[position]

        // Acceder a las vistas mediante View Binding (holder.binding.vista)
        holder.binding.tvNombrePaciente.text = paciente.nombres
        holder.binding.tvDniPaciente.text = "DNI: ${paciente.dni}"
        holder.binding.tvTelefonoPaciente.text = "Tel: ${paciente.telefono}"

        // Evento botón EDITAR
        holder.binding.btnEditarPaciente.setOnClickListener {
            onEditar(paciente)
        }

        // Evento botón ELIMINAR
        holder.binding.btnEliminarPaciente.setOnClickListener {
            onEliminar(paciente)
        }
    }

    // Retorna la cantidad de pacientes en la lista
    override fun getItemCount(): Int = listaPacientes.size

    // Método para actualizar la lista (usado en búsqueda y al refrescar)
    fun actualizarLista(nuevaLista: ArrayList<Paciente>) {
        listaPacientes = nuevaLista
        notifyDataSetChanged()  // Notifica al RecyclerView que los datos cambiaron
    }
}