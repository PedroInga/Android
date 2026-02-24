package com.intoc.sistemacitasmedicas.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.intoc.sistemacitasmedicas.R
import com.intoc.sistemacitasmedicas.adaptadores.AdaptadorCita
import com.intoc.sistemacitasmedicas.bean.Cita
import com.intoc.sistemacitasmedicas.databinding.FragmentListaCitasBinding
import com.intoc.sistemacitasmedicas.db.DatabaseHelper

// Fragment que muestra la lista de citas médicas
// Usa: View Binding, RecyclerView con AdaptadorCita, SQLite vía DatabaseHelper
// Navegación con Navigation Component
class ListaCitasFragment : Fragment() {

    // View Binding (nullable para fragments)
    private var _binding: FragmentListaCitasBinding? = null
    private val binding get() = _binding!!

    // Helper de base de datos SQLite
    private lateinit var dbHelper: DatabaseHelper

    // Adaptador del RecyclerView
    private lateinit var adaptador: AdaptadorCita

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar layout con View Binding
        _binding = FragmentListaCitasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Configurar RecyclerView con LinearLayoutManager vertical
        binding.rvCitas.layoutManager = LinearLayoutManager(requireContext())

        // Crear adaptador con los tres callbacks: ver, editar, eliminar
        adaptador = AdaptadorCita(
            arrayListOf(),

            // ===== CALLBACK VER DETALLE =====
            // Navega a DetalleCitaFragment pasando todos los datos de la cita
            onVer = { cita ->
                val bundle = Bundle().apply {
                    putInt("codigo", cita.codigo)
                    putInt("codigoPaciente", cita.codigoPaciente)
                    putString("nombrePaciente", cita.nombrePaciente)
                    putInt("codigoDoctor", cita.codigoDoctor)
                    putString("nombreDoctor", cita.nombreDoctor)
                    putString("fecha", cita.fecha)
                    putString("hora", cita.hora)
                    putString("motivo", cita.motivo)
                    putString("estado", cita.estado)
                }
                // Navegar usando Navigation Component
                findNavController().navigate(
                    R.id.action_listaCitas_to_detalleCita, bundle
                )
            },

            // ===== CALLBACK EDITAR =====
            // Navega a RegistroCitaFragment en modo edición pasando los datos
            onEditar = { cita ->
                val bundle = Bundle().apply {
                    putInt("codigo", cita.codigo)
                    putInt("codigoPaciente", cita.codigoPaciente)
                    putInt("codigoDoctor", cita.codigoDoctor)
                    putString("fecha", cita.fecha)
                    putString("hora", cita.hora)
                    putString("motivo", cita.motivo)
                    putString("estado", cita.estado)
                }
                // Navegar usando Navigation Component
                findNavController().navigate(
                    R.id.action_listaCitas_to_registroCita, bundle
                )
            },

            // ===== CALLBACK ELIMINAR =====
            // Muestra diálogo de confirmación y elimina la cita de SQLite
            onEliminar = { cita ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Cita")
                    .setMessage("¿Está seguro de eliminar la cita de ${cita.nombrePaciente}?")
                    .setPositiveButton("Sí") { _, _ ->
                        // Eliminar de la base de datos SQLite
                        val resultado = dbHelper.eliminarCita(cita.codigo)
                        if (resultado > 0) {
                            Toast.makeText(requireContext(), "Cita eliminada correctamente", Toast.LENGTH_SHORT).show()
                            // Recargar la lista después de eliminar
                            cargarCitas()
                        } else {
                            Toast.makeText(requireContext(), "Error al eliminar la cita", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        // Asignar adaptador al RecyclerView
        binding.rvCitas.adapter = adaptador

        // ===== FAB: Agregar nueva cita =====
        // Navega a RegistroCitaFragment sin datos (modo agregar)
        binding.fabAgregarCita.setOnClickListener {
            findNavController().navigate(R.id.action_listaCitas_to_registroCita)
        }

        // ===== BÚSQUEDA EN TIEMPO REAL =====
        // TextWatcher que filtra citas por nombre de paciente mientras se escribe
        binding.etBuscarCita.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                if (texto.isNotEmpty()) {
                    // Buscar citas por nombre de paciente en SQLite
                    val lista = dbHelper.buscarCitas(texto)
                    actualizarLista(lista)
                } else {
                    // Si el campo está vacío, cargar todas las citas
                    cargarCitas()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Cargar todas las citas al iniciar
        cargarCitas()
    }

    // Cargar todas las citas desde SQLite
    private fun cargarCitas() {
        val lista = dbHelper.listarCitas()
        actualizarLista(lista)
    }

    // Actualizar la lista del RecyclerView y mostrar/ocultar mensaje vacío
    private fun actualizarLista(lista: ArrayList<Cita>) {
        adaptador.actualizarLista(lista)

        // Actualizar contador de citas
        binding.tvContadorCitas.text = "Total: ${lista.size} citas"

        // Mostrar/ocultar mensaje "No hay citas registradas"
        if (lista.isEmpty()) {
            binding.tvSinCitas.visibility = View.VISIBLE
            binding.rvCitas.visibility = View.GONE
        } else {
            binding.tvSinCitas.visibility = View.GONE
            binding.rvCitas.visibility = View.VISIBLE
        }
    }

    // Recargar la lista al volver al fragment
    override fun onResume() {
        super.onResume()
        cargarCitas()
    }

    // Limpiar binding al destruir la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}