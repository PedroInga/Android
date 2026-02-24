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
import com.intoc.sistemacitasmedicas.adaptadores.AdaptadorDoctor
import com.intoc.sistemacitasmedicas.bean.Doctor
import com.intoc.sistemacitasmedicas.databinding.FragmentListaDoctoresBinding
import com.intoc.sistemacitasmedicas.db.DatabaseHelper

// Fragment que muestra la lista de doctores
// Usa: View Binding, RecyclerView, DatabaseHelper (SQLite), Navigation Component
class ListaDoctoresFragment : Fragment() {

    // View Binding
    private var _binding: FragmentListaDoctoresBinding? = null
    private val binding get() = _binding!!

    // DatabaseHelper para operaciones CRUD con SQLite
    private lateinit var dbHelper: DatabaseHelper

    // Adaptador del RecyclerView
    private lateinit var adaptador: AdaptadorDoctor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaDoctoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Configurar RecyclerView
        binding.rvDoctores.layoutManager = LinearLayoutManager(requireContext())

        // Crear adaptador con callbacks para editar y eliminar
        adaptador = AdaptadorDoctor(
            arrayListOf(),

            // Callback EDITAR: navegar a RegistroDoctorFragment con datos
            onEditar = { doctor ->
                val bundle = Bundle().apply {
                    putInt("codigo", doctor.codigo)
                    putString("nombres", doctor.nombres)
                    putString("especialidad", doctor.especialidad)
                    putString("colegiatura", doctor.colegiatura)
                    putString("telefono", doctor.telefono)
                    putString("correo", doctor.correo)
                }
                findNavController().navigate(
                    R.id.action_listaDoctores_to_registroDoctor, bundle
                )
            },

            // Callback ELIMINAR: diálogo de confirmación + eliminar de SQLite
            onEliminar = { doctor ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Doctor")
                    .setMessage("¿Está seguro de eliminar al Dr. ${doctor.nombres}?")
                    .setPositiveButton("Sí") { _, _ ->
                        // Eliminar directamente de SQLite
                        val resultado = dbHelper.eliminarDoctor(doctor.codigo)
                        if (resultado > 0) {
                            Toast.makeText(requireContext(), "Doctor eliminado", Toast.LENGTH_SHORT).show()
                            cargarDoctores()
                        } else {
                            Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        // Asignar adaptador al RecyclerView
        binding.rvDoctores.adapter = adaptador

        // FAB: navegar a RegistroDoctorFragment
        binding.fabAgregarDoctor.setOnClickListener {
            findNavController().navigate(R.id.action_listaDoctores_to_registroDoctor)
        }

        // Búsqueda en tiempo real
        binding.etBuscarDoctor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                if (texto.isNotEmpty()) {
                    buscarDoctores(texto)
                } else {
                    cargarDoctores()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Cargar doctores al iniciar
        cargarDoctores()
    }

    // Cargar todos los doctores desde SQLite
    private fun cargarDoctores() {
        val lista = dbHelper.listarDoctores()
        actualizarLista(lista)
    }

    // Buscar doctores por nombre desde SQLite
    private fun buscarDoctores(nombre: String) {
        val lista = dbHelper.buscarDoctores(nombre)
        actualizarLista(lista)
    }

    // Actualizar RecyclerView y mostrar/ocultar mensaje vacío
    private fun actualizarLista(lista: ArrayList<Doctor>) {
        adaptador.actualizarLista(lista)
        binding.tvContadorDoctores.text = "Total: ${lista.size} doctores"

        if (lista.isEmpty()) {
            binding.tvSinDoctores.visibility = View.VISIBLE
            binding.rvDoctores.visibility = View.GONE
        } else {
            binding.tvSinDoctores.visibility = View.GONE
            binding.rvDoctores.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDoctores()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}