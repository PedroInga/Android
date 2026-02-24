package com.intoc.sistemacitasmedicas.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intoc.sistemacitasmedicas.bean.Doctor
import com.intoc.sistemacitasmedicas.databinding.FragmentRegistroDoctorBinding
import com.intoc.sistemacitasmedicas.db.DatabaseHelper

// Fragment para registrar o editar un doctor
// Usa: View Binding, Navigation Component, DatabaseHelper (SQLite)
class RegistroDoctorFragment : Fragment() {

    // View Binding
    private var _binding: FragmentRegistroDoctorBinding? = null
    private val binding get() = _binding!!

    // DatabaseHelper para CRUD
    private lateinit var dbHelper: DatabaseHelper

    // Variables para modo edición
    private var codigoEditar: Int = 0
    private var modoEdicion: Boolean = false

    // Lista de especialidades para el Spinner
    private val especialidades = arrayOf(
        "Seleccione especialidad",
        "Medicina General",
        "Cardiología",
        "Dermatología",
        "Gastroenterología",
        "Ginecología",
        "Neurología",
        "Oftalmología",
        "Pediatría",
        "Traumatología",
        "Urología",
        "Otorrinolaringología",
        "Psiquiatría",
        "Endocrinología",
        "Neumología",
        "Oncología"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Configurar Spinner de especialidades
        val adapterSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            especialidades
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spEspecialidad.adapter = adapterSpinner

        // Recibir argumentos (modo edición)
        arguments?.let { args ->
            codigoEditar = args.getInt("codigo", 0)

            if (codigoEditar > 0) {
                modoEdicion = true

                // Cargar datos del doctor en los campos
                binding.etNombreDoctor.setText(args.getString("nombres", ""))
                binding.etCmpDoctor.setText(args.getString("colegiatura", ""))
                binding.etTelefonoDoctor.setText(args.getString("telefono", ""))
                binding.etEmailDoctor.setText(args.getString("correo", ""))

                // Seleccionar especialidad en el Spinner
                val especialidad = args.getString("especialidad", "")
                val posicion = especialidades.indexOf(especialidad)
                if (posicion >= 0) {
                    binding.spEspecialidad.setSelection(posicion)
                }

                // Cambiar texto del botón
                binding.btnGuardarDoctor.text = "Actualizar"
            }
        }

        // Botón GUARDAR / ACTUALIZAR
        binding.btnGuardarDoctor.setOnClickListener {
            val nombres = binding.etNombreDoctor.text.toString().trim()
            val cmp = binding.etCmpDoctor.text.toString().trim()
            val telefono = binding.etTelefonoDoctor.text.toString().trim()
            val correo = binding.etEmailDoctor.text.toString().trim()
            val especialidad = binding.spEspecialidad.selectedItem.toString()

            // Validaciones
            if (nombres.isEmpty()) {
                binding.etNombreDoctor.error = "Ingrese el nombre"
                binding.etNombreDoctor.requestFocus()
                return@setOnClickListener
            }
            if (cmp.isEmpty()) {
                binding.etCmpDoctor.error = "Ingrese el CMP"
                binding.etCmpDoctor.requestFocus()
                return@setOnClickListener
            }
            if (binding.spEspecialidad.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Seleccione una especialidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (telefono.isEmpty() || telefono.length != 9) {
                binding.etTelefonoDoctor.error = "Teléfono debe tener 9 dígitos"
                binding.etTelefonoDoctor.requestFocus()
                return@setOnClickListener
            }

            // Crear objeto Doctor
            val doctor = Doctor(
                codigo = codigoEditar,
                nombres = nombres,
                especialidad = especialidad,
                colegiatura = cmp,
                telefono = telefono,
                correo = correo
            )

            if (modoEdicion) {
                // Actualizar en SQLite
                val resultado = dbHelper.actualizarDoctor(doctor)
                if (resultado > 0) {
                    Toast.makeText(requireContext(), "Doctor actualizado", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Agregar nuevo en SQLite
                val resultado = dbHelper.agregarDoctor(doctor)
                if (resultado > 0) {
                    Toast.makeText(requireContext(), "Doctor registrado", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error al registrar", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Botón LIMPIAR
        binding.btnLimpiarDoctor.setOnClickListener {
            binding.etNombreDoctor.setText("")
            binding.etCmpDoctor.setText("")
            binding.etTelefonoDoctor.setText("")
            binding.etEmailDoctor.setText("")
            binding.spEspecialidad.setSelection(0)
            binding.etNombreDoctor.requestFocus()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}