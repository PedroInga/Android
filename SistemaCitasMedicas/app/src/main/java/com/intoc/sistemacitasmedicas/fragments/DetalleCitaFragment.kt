package com.intoc.sistemacitasmedicas.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intoc.sistemacitasmedicas.R
import com.intoc.sistemacitasmedicas.databinding.FragmentDetalleCitaBinding
import com.intoc.sistemacitasmedicas.db.DatabaseHelper

// Fragment que muestra el detalle completo de una cita médica
// Usa: View Binding, recibe datos vía arguments (Navigation Component)
// Muestra info del paciente, doctor y detalles de la cita
// Permite editar o eliminar la cita desde esta pantalla
class DetalleCitaFragment : Fragment() {

    // View Binding (nullable para fragments)
    private var _binding: FragmentDetalleCitaBinding? = null
    private val binding get() = _binding!!

    // Helper de base de datos SQLite
    private lateinit var dbHelper: DatabaseHelper

    // Variables para almacenar los datos de la cita recibidos por arguments
    private var codigoCita: Int = 0
    private var codigoPaciente: Int = 0
    private var nombrePaciente: String = ""
    private var codigoDoctor: Int = 0
    private var nombreDoctor: String = ""
    private var fecha: String = ""
    private var hora: String = ""
    private var motivo: String = ""
    private var estado: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar layout con View Binding
        _binding = FragmentDetalleCitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // ===== RECIBIR DATOS DE LOS ARGUMENTS =====
        arguments?.let { args ->
            codigoCita = args.getInt("codigo", 0)
            codigoPaciente = args.getInt("codigoPaciente", 0)
            nombrePaciente = args.getString("nombrePaciente", "")
            codigoDoctor = args.getInt("codigoDoctor", 0)
            nombreDoctor = args.getString("nombreDoctor", "")
            fecha = args.getString("fecha", "")
            hora = args.getString("hora", "")
            motivo = args.getString("motivo", "")
            estado = args.getString("estado", "Pendiente")
        }

        // ===== MOSTRAR DATOS EN LAS VISTAS =====
        mostrarDatos()

        // ===== EVENTO: Botón Editar =====
        // Navega a RegistroCitaFragment en modo edición con los datos de la cita
        binding.btnEditarDetalleCita.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("codigo", codigoCita)
                putInt("codigoPaciente", codigoPaciente)
                putInt("codigoDoctor", codigoDoctor)
                putString("fecha", fecha)
                putString("hora", hora)
                putString("motivo", motivo)
                putString("estado", estado)
            }
            findNavController().navigate(
                R.id.action_detalleCita_to_registroCita, bundle
            )
        }

        // ===== EVENTO: Botón Eliminar =====
        // Muestra diálogo de confirmación y elimina la cita
        binding.btnEliminarDetalleCita.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Cita")
                .setMessage("¿Está seguro de eliminar esta cita?")
                .setPositiveButton("Sí") { _, _ ->
                    val resultado = dbHelper.eliminarCita(codigoCita)
                    if (resultado > 0) {
                        Toast.makeText(requireContext(), "Cita eliminada correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()  // Volver a la lista
                    } else {
                        Toast.makeText(requireContext(), "Error al eliminar la cita", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    // ===== MOSTRAR TODOS LOS DATOS DE LA CITA =====
    private fun mostrarDatos() {
        // --- Estado con badge de color ---
        binding.tvEstadoDetalle.text = estado
        when (estado) {
            "Pendiente" -> binding.tvEstadoDetalle.setBackgroundResource(R.drawable.bg_estado_pendiente)
            "Confirmada" -> binding.tvEstadoDetalle.setBackgroundResource(R.drawable.bg_estado_confirmada)
            "Completada" -> binding.tvEstadoDetalle.setBackgroundResource(R.drawable.bg_estado_completada)
            "Cancelada" -> binding.tvEstadoDetalle.setBackgroundResource(R.drawable.bg_estado_cancelada)
        }

        // --- Información del Paciente ---
        binding.tvDetallePaciente.text = nombrePaciente

        // Buscar datos adicionales del paciente en SQLite (DNI, teléfono)
        val paciente = dbHelper.obtenerPacientePorCodigo(codigoPaciente)
        if (paciente != null) {
            binding.tvDetalleDniPaciente.text = "DNI: ${paciente.dni}"
            binding.tvDetalleTelPaciente.text = "Tel: ${paciente.telefono}"
        } else {
            binding.tvDetalleDniPaciente.text = "DNI: No disponible"
            binding.tvDetalleTelPaciente.text = "Tel: No disponible"
        }

        // --- Información del Doctor ---
        binding.tvDetalleDoctor.text = "Dr. $nombreDoctor"

        // Buscar datos adicionales del doctor en SQLite (especialidad)
        val doctor = dbHelper.obtenerDoctorPorCodigo(codigoDoctor)
        if (doctor != null) {
            binding.tvDetalleEspecialidad.text = doctor.especialidad
        } else {
            binding.tvDetalleEspecialidad.text = "Especialidad: No disponible"
        }

        // --- Detalles de la Cita ---
        binding.tvDetalleFecha.text = "Fecha: $fecha"
        binding.tvDetalleHora.text = "Hora: $hora"
        binding.tvDetalleMotivo.text = motivo
    }

    // Limpiar binding al destruir la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}