package com.intoc.sistemacitasmedicas.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intoc.sistemacitasmedicas.api.ApiManager
import com.intoc.sistemacitasmedicas.bean.Cita
import com.intoc.sistemacitasmedicas.bean.Doctor
import com.intoc.sistemacitasmedicas.bean.Paciente
import com.intoc.sistemacitasmedicas.databinding.FragmentRegistroCitaBinding
import com.intoc.sistemacitasmedicas.db.DatabaseHelper
import java.util.Calendar

/**
 * =====================================================
 * RegistroCitaFragment.kt
 * =====================================================
 * Fragment para registrar o editar una cita médica.
 *
 * FUNCIONALIDADES:
 * 1) Spinners dinámicos (pacientes y doctores desde SQLite)
 * 2) DatePickerDialog para seleccionar fecha
 * 3) TimePickerDialog para seleccionar hora
 * 4) Validación de feriados con API Nager.Date ← NUEVO
 *    Al seleccionar una fecha, consulta la API para verificar
 *    si es feriado y muestra una advertencia al usuario.
 * 5) CRUD de citas (agregar/editar) en SQLite
 *
 * Si recibe "codigo" > 0 en los argumentos, es modo EDICIÓN.
 *
 * TEMAS DEL SÍLABO CUBIERTOS:
 * - Unidad 4: Consumo de servicios web REST (Retrofit)
 * - Unidad 3: Persistencia de datos (SQLite)
 * - Unidad 2: Componentes de UI (Spinners, Dialogs)
 * - Unidad 1: Activities, Intents, Navigation Component
 * =====================================================
 */
class RegistroCitaFragment : Fragment() {

    // View Binding (nullable para fragments)
    private var _binding: FragmentRegistroCitaBinding? = null
    private val binding get() = _binding!!

    // Helper de base de datos SQLite
    private lateinit var dbHelper: DatabaseHelper

    // Manager de APIs REST (MediCitas + Nager.Date)
    private lateinit var apiManager: ApiManager

    // Variables para modo edición
    private var codigoEditar: Int = 0       // 0 = agregar, >0 = editar
    private var modoEdicion: Boolean = false

    // Listas de pacientes y doctores para los Spinners
    private var listaPacientes = ArrayList<Paciente>()
    private var listaDoctores = ArrayList<Doctor>()

    // Estados posibles de una cita
    private val estados = arrayOf("Pendiente", "Confirmada", "Completada", "Cancelada")

    // Flag para saber si la fecha seleccionada es feriado
    // Se usa para mostrar advertencia antes de guardar
    private var fechaEsFeriado: Boolean = false
    private var nombreFeriado: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar layout con View Binding
        _binding = FragmentRegistroCitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar DatabaseHelper y ApiManager
        dbHelper = DatabaseHelper(requireContext())
        apiManager = ApiManager(requireContext())

        // ===== CARGAR SPINNERS DINÁMICOS =====
        cargarSpinnerPacientes()
        cargarSpinnerDoctores()
        cargarSpinnerEstados()

        // ===== RECIBIR ARGUMENTOS DEL NAVIGATION COMPONENT (modo edición) =====
        arguments?.let { args ->
            codigoEditar = args.getInt("codigo", 0)

            if (codigoEditar > 0) {
                modoEdicion = true

                // Cargar datos de la cita en los campos
                val codigoPaciente = args.getInt("codigoPaciente", 0)
                val codigoDoctor = args.getInt("codigoDoctor", 0)
                val fecha = args.getString("fecha", "")
                val hora = args.getString("hora", "")
                val motivo = args.getString("motivo", "")
                val estado = args.getString("estado", "Pendiente")

                // Establecer fecha, hora y motivo
                binding.etFechaCita.setText(fecha)
                binding.etHoraCita.setText(hora)
                binding.etMotivoCita.setText(motivo)

                // Seleccionar el paciente correcto en el Spinner
                for (i in listaPacientes.indices) {
                    if (listaPacientes[i].codigo == codigoPaciente) {
                        binding.spPacienteCita.setSelection(i + 1)
                        break
                    }
                }

                // Seleccionar el doctor correcto en el Spinner
                for (i in listaDoctores.indices) {
                    if (listaDoctores[i].codigo == codigoDoctor) {
                        binding.spDoctorCita.setSelection(i + 1)
                        break
                    }
                }

                // Seleccionar el estado correcto en el Spinner
                val posEstado = estados.indexOf(estado)
                if (posEstado >= 0) {
                    binding.spEstadoCita.setSelection(posEstado)
                }

                // Cambiar texto del botón a "Actualizar"
                binding.btnGuardarCita.text = "Actualizar"
            }
        }

        // ===== EVENTO: Seleccionar fecha con DatePickerDialog =====
        binding.etFechaCita.setOnClickListener {
            mostrarDatePicker()
        }

        // ===== EVENTO: Seleccionar hora con TimePickerDialog =====
        binding.etHoraCita.setOnClickListener {
            mostrarTimePicker()
        }

        // ===== EVENTO: Botón Guardar/Actualizar =====
        binding.btnGuardarCita.setOnClickListener {
            guardarCita()
        }

        // ===== EVENTO: Botón Limpiar =====
        binding.btnLimpiarCita.setOnClickListener {
            limpiarCampos()
        }
    }

    // ===== CARGAR SPINNER DE PACIENTES DESDE SQLITE =====
    private fun cargarSpinnerPacientes() {
        listaPacientes = dbHelper.listarPacientes()

        val nombresPacientes = ArrayList<String>()
        nombresPacientes.add("Seleccione paciente")
        for (paciente in listaPacientes) {
            nombresPacientes.add(paciente.nombres)
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresPacientes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPacienteCita.adapter = adapter
    }

    // ===== CARGAR SPINNER DE DOCTORES DESDE SQLITE =====
    private fun cargarSpinnerDoctores() {
        listaDoctores = dbHelper.listarDoctores()

        val nombresDoctores = ArrayList<String>()
        nombresDoctores.add("Seleccione doctor")
        for (doctor in listaDoctores) {
            nombresDoctores.add("Dr. ${doctor.nombres}")
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresDoctores
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDoctorCita.adapter = adapter
    }

    // ===== CARGAR SPINNER DE ESTADOS =====
    private fun cargarSpinnerEstados() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            estados
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spEstadoCita.adapter = adapter
    }

    // =====================================================
    // ===== DATEPICKER CON VALIDACIÓN DE FERIADOS ===== ACTUALIZADO
    // =====================================================
    /**
     * Abre un DatePickerDialog para seleccionar la fecha.
     * Después de seleccionar, consulta la API Nager.Date
     * para verificar si la fecha es un feriado en Perú.
     *
     * Si es feriado, muestra un AlertDialog de advertencia
     * indicando el nombre del feriado, pero permite continuar.
     */
    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Formatear fecha como dd/MM/yyyy
                val fechaFormateada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                binding.etFechaCita.setText(fechaFormateada)

                // Resetear flag de feriado
                fechaEsFeriado = false
                nombreFeriado = ""

                // ===== VALIDAR SI ES FERIADO (API Nager.Date) =====
                // Consulta asíncrona a la API pública de feriados
                verificarFeriado(fechaFormateada, year)
            },
            anio, mes, dia
        )

        // No permitir fechas pasadas
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    /**
     * Verifica si la fecha seleccionada es un feriado en Perú.
     * Usa ApiManager → Nager.Date API.
     *
     * Si es feriado:
     * - Muestra un AlertDialog con el nombre del feriado
     * - Marca el flag fechaEsFeriado = true
     * - El usuario puede decidir si continúa o cambia la fecha
     *
     * Si NO es feriado o la API falla:
     * - No muestra nada, el flujo continúa normal
     *
     * @param fecha Fecha en formato "dd/MM/yyyy"
     * @param year Año seleccionado
     */
    private fun verificarFeriado(fecha: String, year: Int) {
        apiManager.checkIfHoliday(fecha, year,
            onResult = { feriadoNombre ->
                // Verificar que el fragment siga activo
                if (!isAdded || _binding == null) return@checkIfHoliday

                requireActivity().runOnUiThread {
                    if (feriadoNombre != null) {
                        // ¡La fecha es un feriado!
                        fechaEsFeriado = true
                        nombreFeriado = feriadoNombre

                        // Mostrar AlertDialog de advertencia
                        AlertDialog.Builder(requireContext())
                            .setTitle("⚠️ Fecha es Feriado")
                            .setMessage(
                                "La fecha seleccionada ($fecha) es:\n\n" +
                                        "🎉 $feriadoNombre\n\n" +
                                        "Es posible que la clínica no atienda este día.\n" +
                                        "¿Desea mantener esta fecha?"
                            )
                            .setPositiveButton("Sí, mantener") { dialog, _ ->
                                // El usuario acepta la fecha feriado
                                dialog.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    "⚠️ Recuerde: $fecha es $feriadoNombre",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .setNegativeButton("No, cambiar fecha") { dialog, _ ->
                                // Limpiar la fecha para que seleccione otra
                                binding.etFechaCita.setText("")
                                fechaEsFeriado = false
                                nombreFeriado = ""
                                dialog.dismiss()
                            }
                            .setCancelable(false)
                            .show()
                    }
                    // Si feriadoNombre es null, no es feriado → no hacer nada
                }
            },
            onError = { error ->
                // Si la API falla, no bloquear al usuario
                // Solo registrar en log, el flujo continúa normal
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        // Opcional: mostrar un Toast discreto
                        // Toast.makeText(requireContext(),
                        //     "No se pudo verificar feriados", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // ===== MOSTRAR TIMEPICKERDIALOG =====
    private fun mostrarTimePicker() {
        val calendario = Calendar.getInstance()
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val horaFormateada = String.format("%02d:%02d", hourOfDay, minute)
                binding.etHoraCita.setText(horaFormateada)
            },
            hora, minuto, true
        )
        timePicker.show()
    }

    // =====================================================
    // ===== GUARDAR CITA CON ADVERTENCIA DE FERIADO ===== ACTUALIZADO
    // =====================================================
    /**
     * Guarda o actualiza la cita en SQLite.
     * Si la fecha es feriado, muestra una confirmación adicional
     * antes de guardar definitivamente.
     */
    private fun guardarCita() {
        // Obtener datos de los campos
        val posPaciente = binding.spPacienteCita.selectedItemPosition
        val posDoctor = binding.spDoctorCita.selectedItemPosition
        val fecha = binding.etFechaCita.text.toString().trim()
        val hora = binding.etHoraCita.text.toString().trim()
        val motivo = binding.etMotivoCita.text.toString().trim()
        val estado = binding.spEstadoCita.selectedItem.toString()

        // ===== VALIDACIONES =====
        if (posPaciente == 0) {
            Toast.makeText(requireContext(), "Seleccione un paciente", Toast.LENGTH_SHORT).show()
            return
        }

        if (posDoctor == 0) {
            Toast.makeText(requireContext(), "Seleccione un doctor", Toast.LENGTH_SHORT).show()
            return
        }

        if (fecha.isEmpty()) {
            binding.etFechaCita.error = "Seleccione la fecha"
            binding.etFechaCita.requestFocus()
            return
        }

        if (hora.isEmpty()) {
            binding.etHoraCita.error = "Seleccione la hora"
            binding.etHoraCita.requestFocus()
            return
        }

        if (motivo.isEmpty()) {
            binding.etMotivoCita.error = "Ingrese el motivo de la consulta"
            binding.etMotivoCita.requestFocus()
            return
        }

        // Obtener los códigos del paciente y doctor seleccionados
        val codigoPaciente = listaPacientes[posPaciente - 1].codigo
        val codigoDoctor = listaDoctores[posDoctor - 1].codigo

        // Crear objeto Cita con los datos del formulario
        val cita = Cita(
            codigo = codigoEditar,
            codigoPaciente = codigoPaciente,
            codigoDoctor = codigoDoctor,
            fecha = fecha,
            hora = hora,
            motivo = motivo,
            estado = estado
        )

        // ===== VERIFICACIÓN FINAL DE FERIADO ANTES DE GUARDAR =====
        if (fechaEsFeriado) {
            // Mostrar confirmación adicional si la fecha es feriado
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Confirmar Cita en Feriado")
                .setMessage(
                    "Está a punto de ${if (modoEdicion) "actualizar" else "registrar"} " +
                            "una cita para el $fecha.\n\n" +
                            "Esta fecha es feriado: 🎉 $nombreFeriado\n\n" +
                            "¿Desea continuar?"
                )
                .setPositiveButton("Sí, guardar") { dialog, _ ->
                    dialog.dismiss()
                    // Proceder a guardar la cita
                    ejecutarGuardado(cita)
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            // No es feriado, guardar directamente
            ejecutarGuardado(cita)
        }
    }

    /**
     * Ejecuta el guardado/actualización de la cita en SQLite.
     * Se separa del método guardarCita() para poder llamarlo
     * tanto directamente como desde el AlertDialog de feriado.
     *
     * @param cita Objeto Cita con todos los datos del formulario
     */
    private fun ejecutarGuardado(cita: Cita) {
        if (modoEdicion) {
            // ===== MODO EDICIÓN: Actualizar cita en SQLite =====
            val resultado = dbHelper.actualizarCita(cita)
            if (resultado > 0) {
                Toast.makeText(
                    requireContext(),
                    "Cita actualizada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error al actualizar la cita",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // ===== MODO AGREGAR: Insertar nueva cita en SQLite =====
            val resultado = dbHelper.agregarCita(cita)
            if (resultado > 0) {
                Toast.makeText(
                    requireContext(),
                    "Cita registrada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error al registrar la cita",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ===== LIMPIAR TODOS LOS CAMPOS =====
    private fun limpiarCampos() {
        binding.spPacienteCita.setSelection(0)
        binding.spDoctorCita.setSelection(0)
        binding.etFechaCita.setText("")
        binding.etHoraCita.setText("")
        binding.etMotivoCita.setText("")
        binding.spEstadoCita.setSelection(0)

        // Resetear flags de feriado
        fechaEsFeriado = false
        nombreFeriado = ""
    }

    // Limpiar binding al destruir la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}