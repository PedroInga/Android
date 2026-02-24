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
import com.intoc.sistemacitasmedicas.adaptadores.AdaptadorPaciente
import com.intoc.sistemacitasmedicas.api.ApiManager
import com.intoc.sistemacitasmedicas.bean.Paciente
import com.intoc.sistemacitasmedicas.databinding.FragmentListaPacientesBinding

// Fragment que muestra la lista de pacientes
// Usa: View Binding, RecyclerView con Adapter/ViewHolder, API REST con fallback SQLite
// Navegación con Navigation Component
class ListaPacientesFragment : Fragment() {

    // View Binding (nullable para evitar memory leaks en fragments)
    private var _binding: FragmentListaPacientesBinding? = null
    private val binding get() = _binding!!

    // ApiManager: capa que maneja API REST + SQLite como fallback
    private lateinit var apiManager: ApiManager

    // Adaptador del RecyclerView (usa ViewHolder internamente)
    private lateinit var adaptador: AdaptadorPaciente

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar layout con View Binding
        _binding = FragmentListaPacientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ApiManager (maneja API + SQLite)
        apiManager = ApiManager(requireContext())

        // Configurar RecyclerView con LinearLayoutManager
        binding.rvPacientes.layoutManager = LinearLayoutManager(requireContext())

        // Crear adaptador con callbacks para editar y eliminar
        adaptador = AdaptadorPaciente(
            arrayListOf(),

            // Callback EDITAR: navegar a RegistroPacienteFragment con datos del paciente
            onEditar = { paciente ->
                // Crear Bundle con los datos del paciente para pasar al fragment de registro
                val bundle = Bundle().apply {
                    putInt("codigo", paciente.codigo)
                    putString("nombres", paciente.nombres)
                    putString("apellidos", paciente.apellidos)
                    putString("dni", paciente.dni)
                    putString("telefono", paciente.telefono)
                    putString("correo", paciente.correo)
                }
                // Navegar usando Navigation Component con el bundle de argumentos
                findNavController().navigate(
                    R.id.action_listaPacientes_to_registroPaciente, bundle
                )
            },

            // Callback ELIMINAR: mostrar diálogo de confirmación y eliminar vía API
            onEliminar = { paciente ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Paciente")
                    .setMessage("¿Está seguro de eliminar a ${paciente.nombres} ${paciente.apellidos}?")
                    .setPositiveButton("Sí") { _, _ ->
                        // Llamar a ApiManager para eliminar (intenta API, si falla usa SQLite)
                        apiManager.eliminarPaciente(
                            paciente.codigo,
                            onSuccess = { msg ->
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                // Recargar la lista después de eliminar
                                cargarPacientes()
                            },
                            onError = { msg ->
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        // Asignar adaptador al RecyclerView
        binding.rvPacientes.adapter = adaptador

        // FAB: navegar a RegistroPacienteFragment para agregar nuevo paciente
        binding.fabAgregarPaciente.setOnClickListener {
            // Sin bundle = modo agregar (código será 0 por defecto)
            findNavController().navigate(R.id.action_listaPacientes_to_registroPaciente)
        }

        // Búsqueda en tiempo real con TextWatcher
        binding.etBuscarPaciente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                if (texto.isNotEmpty()) {
                    // Buscar pacientes por nombre vía API/SQLite
                    buscarPacientes(texto)
                } else {
                    // Si el campo está vacío, cargar todos los pacientes
                    cargarPacientes()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Cargar pacientes al iniciar el fragment
        cargarPacientes()
    }

    // Cargar todos los pacientes desde API (con fallback a SQLite)
    private fun cargarPacientes() {
        apiManager.listarPacientes(
            onSuccess = { lista ->
                actualizarLista(lista)
            },
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Buscar pacientes por nombre
    private fun buscarPacientes(nombre: String) {
        apiManager.buscarPacientes(
            nombre,
            onSuccess = { lista ->
                actualizarLista(lista)
            },
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Actualizar la lista del RecyclerView y mostrar/ocultar mensaje vacío
    private fun actualizarLista(lista: ArrayList<Paciente>) {
        adaptador.actualizarLista(lista)

        // Actualizar contador de pacientes
        binding.tvContadorPacientes.text = "Total: ${lista.size} pacientes"

        // Mostrar/ocultar mensaje "No hay pacientes"
        if (lista.isEmpty()) {
            binding.tvSinPacientes.visibility = View.VISIBLE
            binding.rvPacientes.visibility = View.GONE
        } else {
            binding.tvSinPacientes.visibility = View.GONE
            binding.rvPacientes.visibility = View.VISIBLE
        }
    }

    // Se ejecuta cada vez que el fragment vuelve a ser visible
    // Recarga la lista por si se agregó/editó un paciente
    override fun onResume() {
        super.onResume()
        cargarPacientes()
    }

    // Limpiar binding al destruir la vista (evitar memory leaks)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}