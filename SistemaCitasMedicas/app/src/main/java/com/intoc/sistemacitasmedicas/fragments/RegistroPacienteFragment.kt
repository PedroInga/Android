package com.intoc.sistemacitasmedicas.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intoc.sistemacitasmedicas.api.ApiManager
import com.intoc.sistemacitasmedicas.bean.Paciente
import com.intoc.sistemacitasmedicas.databinding.FragmentRegistroPacienteBinding

// Fragment para registrar o editar un paciente
// Usa: View Binding, Navigation Component (argumentos), API REST con fallback SQLite
// Si recibe código > 0 en los argumentos, es modo EDICIÓN
// Si código es 0 o no viene, es modo AGREGAR
class RegistroPacienteFragment : Fragment() {

    // View Binding (nullable para fragments)
    private var _binding: FragmentRegistroPacienteBinding? = null
    private val binding get() = _binding!!

    // ApiManager para operaciones CRUD (API + SQLite)
    private lateinit var apiManager: ApiManager

    // Variables para modo edición
    private var codigoEditar: Int = 0       // 0 = agregar, >0 = editar
    private var modoEdicion: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar layout con View Binding
        _binding = FragmentRegistroPacienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ApiManager
        apiManager = ApiManager(requireContext())

        // ===== RECIBIR ARGUMENTOS DEL NAVIGATION COMPONENT =====
        // Si viene de ListaPacientesFragment con datos, es modo edición
        arguments?.let { args ->
            codigoEditar = args.getInt("codigo", 0)

            // Si el código es mayor a 0, estamos editando un paciente existente
            if (codigoEditar > 0) {
                modoEdicion = true

                // Cargar los datos del paciente en los campos
                binding.etNombrePaciente.setText(args.getString("nombres", ""))
                binding.etDniPaciente.setText(args.getString("dni", ""))
                binding.etTelefonoPaciente.setText(args.getString("telefono", ""))
                binding.etEmailPaciente.setText(args.getString("correo", ""))
                binding.etDireccionPaciente.setText(args.getString("apellidos", ""))

                // Cambiar texto del botón a "Actualizar"
                binding.btnGuardarPaciente.text = "Actualizar"
            }
        }

        // ===== EVENTO BOTÓN GUARDAR/ACTUALIZAR =====
        binding.btnGuardarPaciente.setOnClickListener {
            // Obtener datos de los campos usando View Binding
            val nombres = binding.etNombrePaciente.text.toString().trim()
            val dni = binding.etDniPaciente.text.toString().trim()
            val telefono = binding.etTelefonoPaciente.text.toString().trim()
            val correo = binding.etEmailPaciente.text.toString().trim()
            val direccion = binding.etDireccionPaciente.text.toString().trim()

            // ===== VALIDACIONES =====
            if (nombres.isEmpty()) {
                binding.etNombrePaciente.error = "Ingrese el nombre"
                binding.etNombrePaciente.requestFocus()
                return@setOnClickListener
            }

            if (dni.isEmpty() || dni.length != 8) {
                binding.etDniPaciente.error = "DNI debe tener 8 dígitos"
                binding.etDniPaciente.requestFocus()
                return@setOnClickListener
            }

            if (telefono.isEmpty() || telefono.length != 9) {
                binding.etTelefonoPaciente.error = "Teléfono debe tener 9 dígitos"
                binding.etTelefonoPaciente.requestFocus()
                return@setOnClickListener
            }

            // Crear objeto Paciente con los datos del formulario
            val paciente = Paciente(
                codigo = codigoEditar,
                nombres = nombres,
                apellidos = direccion,   // Campo dirección se guarda en apellidos
                dni = dni,
                telefono = telefono,
                correo = correo
            )

            if (modoEdicion) {
                // ===== MODO EDICIÓN: Actualizar paciente vía API =====
                apiManager.actualizarPaciente(
                    paciente,
                    onSuccess = { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        // Volver a la lista usando Navigation Component
                        findNavController().popBackStack()
                    },
                    onError = { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // ===== MODO AGREGAR: Registrar nuevo paciente vía API =====
                apiManager.agregarPaciente(
                    paciente,
                    onSuccess = { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        // Volver a la lista usando Navigation Component
                        findNavController().popBackStack()
                    },
                    onError = { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // ===== EVENTO BOTÓN LIMPIAR =====
        binding.btnLimpiarPaciente.setOnClickListener {
            // Limpiar todos los campos usando View Binding
            binding.etNombrePaciente.setText("")
            binding.etDniPaciente.setText("")
            binding.etTelefonoPaciente.setText("")
            binding.etEmailPaciente.setText("")
            binding.etDireccionPaciente.setText("")
            binding.etNombrePaciente.requestFocus()
        }
    }

    // Limpiar binding al destruir la vista (evitar memory leaks)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}