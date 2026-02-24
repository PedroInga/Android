package com.intoc.sistemacitasmedicas.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intoc.sistemacitasmedicas.api.ApiManager
import com.intoc.sistemacitasmedicas.api.response.HolidayResponse
import com.intoc.sistemacitasmedicas.databinding.FragmentReportesBinding
import com.intoc.sistemacitasmedicas.db.DatabaseHelper
import java.util.Calendar

/**
 * =====================================================
 * ReportesFragment.kt
 * =====================================================
 * Fragment que muestra reportes y estadísticas del sistema.
 *
 * FUNCIONALIDADES:
 * 1) Estadísticas generales (totales de pacientes, doctores, citas)
 * 2) Citas por estado (pendientes, confirmadas, completadas, canceladas)
 * 3) Ranking de doctores más solicitados (SQLite GROUP BY)
 * 4) Próximos feriados de Perú (API REST Nager.Date)
 *
 * COMPONENTES DEL LAYOUT (fragment_reportes.xml):
 * - tvTotalPacientes, tvTotalDoctores, tvTotalCitas → Tarjetas resumen
 * - tvCitasPendientes/Confirmadas/Completadas/Canceladas → Estados
 * - lvDoctoresTop → ListView ranking doctores
 * - progressFeriados → ProgressBar mientras carga API
 * - tvErrorFeriados → TextView para errores de conexión
 * - lvFeriados → ListView de feriados (inicia GONE)
 * - btnVolverReportes → Botón volver al menú
 *
 * TEMAS DEL SÍLABO CUBIERTOS:
 * - Unidad 4: Consumo de servicios web REST (Retrofit + Gson)
 * - Unidad 4: Procesamiento de JSON (API Nager.Date)
 * - Unidad 3: Persistencia de datos (SQLite consultas)
 * - Unidad 2: Listas y adaptadores (ListView, ArrayAdapter)
 * - Unidad 1: Fragments, Navigation Component
 * =====================================================
 */
class ReportesFragment : Fragment() {

    // View Binding (nullable para fragments - se limpia en onDestroyView)
    private var _binding: FragmentReportesBinding? = null
    private val binding get() = _binding!!

    // Helper de base de datos SQLite para consultas locales
    private lateinit var dbHelper: DatabaseHelper

    // Manager de APIs REST (MediCitas Beeceptor + Nager.Date feriados)
    private lateinit var apiManager: ApiManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar layout con View Binding
        _binding = FragmentReportesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar DatabaseHelper para consultas SQLite
        dbHelper = DatabaseHelper(requireContext())

        // Inicializar ApiManager para llamadas a API REST
        apiManager = ApiManager(requireContext())

        // Cargar todas las estadísticas desde SQLite
        cargarEstadisticas()

        // Cargar lista de doctores más solicitados desde SQLite
        cargarDoctoresTop()

        // Cargar próximos feriados de Perú desde API Nager.Date
        cargarFeriados()

        // ===== EVENTO: Botón Volver al Menú =====
        // Usa Navigation Component para regresar al fragment anterior
        binding.btnVolverReportes.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // ==========================================================
    // ===== CARGAR ESTADÍSTICAS GENERALES DESDE SQLITE =====
    // ==========================================================
    /**
     * Consulta la BD SQLite para obtener totales generales
     * y los muestra en las tarjetas de resumen del layout.
     *
     * Usa métodos de DatabaseHelper:
     * - contarPacientes() → SELECT COUNT(*) FROM pacientes
     * - contarDoctores() → SELECT COUNT(*) FROM doctores
     * - contarCitas() → SELECT COUNT(*) FROM citas
     * - contarCitasPorEstado(estado) → SELECT COUNT(*) WHERE estado = ?
     */
    private fun cargarEstadisticas() {
        // --- Totales generales ---
        // Contar pacientes registrados en SQLite
        val totalPacientes = dbHelper.contarPacientes()
        binding.tvTotalPacientes.text = totalPacientes.toString()

        // Contar doctores registrados en SQLite
        val totalDoctores = dbHelper.contarDoctores()
        binding.tvTotalDoctores.text = totalDoctores.toString()

        // Contar citas registradas en SQLite
        val totalCitas = dbHelper.contarCitas()
        binding.tvTotalCitas.text = totalCitas.toString()

        // --- Citas por estado ---
        // Contar citas pendientes
        val pendientes = dbHelper.contarCitasPorEstado("Pendiente")
        binding.tvCitasPendientes.text = pendientes.toString()

        // Contar citas confirmadas
        val confirmadas = dbHelper.contarCitasPorEstado("Confirmada")
        binding.tvCitasConfirmadas.text = confirmadas.toString()

        // Contar citas completadas
        val completadas = dbHelper.contarCitasPorEstado("Completada")
        binding.tvCitasCompletadas.text = completadas.toString()

        // Contar citas canceladas
        val canceladas = dbHelper.contarCitasPorEstado("Cancelada")
        binding.tvCitasCanceladas.text = canceladas.toString()
    }

    // ==========================================================
    // ===== CARGAR DOCTORES MÁS SOLICITADOS =====
    // ==========================================================
    /**
     * Consulta la BD SQLite con GROUP BY para obtener el ranking
     * de doctores con más citas asignadas.
     *
     * Usa: DatabaseHelper.doctoresMasSolicitados()
     * Retorna: List<Pair<String, Int>> → (nombreDoctor, cantidadCitas)
     *
     * Los resultados se muestran en lvDoctoresTop con ArrayAdapter.
     */
    private fun cargarDoctoresTop() {
        // Obtener lista de pares (nombreDoctor, cantidadCitas) desde SQLite
        val doctoresTop = dbHelper.doctoresMasSolicitados()

        // Crear lista de strings formateados para el ListView
        val listaTexto = ArrayList<String>()
        for ((index, par) in doctoresTop.withIndex()) {
            // Formato: "1. Dr. Nombre (X citas)"
            listaTexto.add("${index + 1}. ${par.first} (${par.second} citas)")
        }

        // Si no hay datos, mostrar mensaje informativo
        if (listaTexto.isEmpty()) {
            listaTexto.add("No hay datos disponibles")
        }

        // Configurar ArrayAdapter simple para el ListView
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listaTexto
        )
        binding.lvDoctoresTop.adapter = adapter
    }

    // ==========================================================
    // ===== CARGAR FERIADOS DESDE API REST NAGER.DATE =====
    // ==========================================================
    /**
     * Consulta la API pública Nager.Date para obtener los
     * próximos feriados públicos de Perú.
     *
     * Endpoint: GET https://date.nager.at/api/v3/NextPublicHolidays/PE
     *
     * FLUJO:
     * 1) Mostrar ProgressBar (progressFeriados) → visible
     * 2) Ocultar ListView (lvFeriados) → gone
     * 3) Ocultar error (tvErrorFeriados) → gone
     * 4) Llamar API con ApiManager.getNextHolidays("PE")
     * 5a) Si éxito → ocultar ProgressBar, mostrar ListView con datos
     * 5b) Si error → ocultar ProgressBar, mostrar tvErrorFeriados
     *     → intentar fallback con feriados del año actual
     *
     * Usa: ApiManager → RetrofitClient.holidayApiService → Nager.Date
     */
    private fun cargarFeriados() {
        // Estado inicial: mostrar ProgressBar, ocultar lista y error
        binding.progressFeriados.visibility = View.VISIBLE
        binding.lvFeriados.visibility = View.GONE
        binding.tvErrorFeriados.visibility = View.GONE

        // Llamar a la API de próximos feriados de Perú (código ISO: "PE")
        apiManager.getNextHolidays("PE",
            onSuccess = { holidays ->
                // Verificar que el fragment siga activo (evitar crash)
                if (!isAdded || _binding == null) return@getNextHolidays

                // Ejecutar en el hilo principal (UI Thread) porque
                // Retrofit ejecuta callbacks en background thread
                requireActivity().runOnUiThread {
                    // Ocultar ProgressBar
                    binding.progressFeriados.visibility = View.GONE

                    if (holidays.isNotEmpty()) {
                        // Mostrar feriados en el ListView
                        mostrarFeriados(holidays)
                    } else {
                        // API respondió pero sin datos
                        mostrarErrorFeriados("No se encontraron feriados próximos")
                    }
                }
            },
            onError = { error ->
                // Verificar que el fragment siga activo
                if (!isAdded || _binding == null) return@getNextHolidays

                requireActivity().runOnUiThread {
                    // Ocultar ProgressBar
                    binding.progressFeriados.visibility = View.GONE

                    // Mostrar mensaje de error
                    mostrarErrorFeriados("⚠️ No se pudieron cargar los feriados\n$error")

                    // Intentar fallback: cargar feriados del año actual
                    cargarFeriadosDelAnio()
                }
            }
        )
    }

    /**
     * FALLBACK: Si getNextHolidays falla, intenta obtener
     * todos los feriados del año actual y filtra los futuros.
     *
     * Endpoint: GET https://date.nager.at/api/v3/PublicHolidays/2026/PE
     *
     * Esto es útil cuando el endpoint NextPublicHolidays no está
     * disponible pero PublicHolidays sí funciona.
     */
    private fun cargarFeriadosDelAnio() {
        // Obtener año actual del calendario del dispositivo
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)

        // Mostrar ProgressBar de nuevo para el segundo intento
        binding.progressFeriados.visibility = View.VISIBLE

        apiManager.getHolidays(anioActual, "PE",
            onSuccess = { holidays ->
                if (!isAdded || _binding == null) return@getHolidays

                // Filtrar solo feriados futuros (desde hoy en adelante)
                val hoy = Calendar.getInstance()
                val feriadosFuturos = holidays.filter { holiday ->
                    try {
                        // Parsear fecha del feriado "yyyy-MM-dd"
                        val parts = holiday.date.split("-")
                        val fechaFeriado = Calendar.getInstance().apply {
                            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        }
                        // Incluir feriados de hoy o futuros
                        fechaFeriado.after(hoy) || esMismoDia(fechaFeriado, hoy)
                    } catch (e: Exception) {
                        false // Si falla el parseo, excluir
                    }
                }

                requireActivity().runOnUiThread {
                    // Ocultar ProgressBar
                    binding.progressFeriados.visibility = View.GONE

                    if (feriadosFuturos.isNotEmpty()) {
                        // Limpiar error previo y mostrar feriados
                        binding.tvErrorFeriados.visibility = View.GONE
                        mostrarFeriados(feriadosFuturos)
                    }
                    // Si tampoco hay datos, mantener el error anterior
                }
            },
            onError = {
                // Ambos intentos fallaron, mantener el error mostrado
                if (!isAdded || _binding == null) return@getHolidays

                requireActivity().runOnUiThread {
                    binding.progressFeriados.visibility = View.GONE
                }
            }
        )
    }

    /**
     * Muestra la lista de feriados en el ListView (lvFeriados).
     * Formatea cada feriado con emoji, fecha en formato dd/MM/yyyy
     * y nombre en español (localName).
     *
     * @param holidays Lista de HolidayResponse obtenidos de la API
     */
    private fun mostrarFeriados(holidays: List<HolidayResponse>) {
        val listaTexto = ArrayList<String>()
        // 🔥 Eliminar duplicados por fecha + nombre
        val feriadosUnicos = holidays.distinctBy {
            it.date + it.localName
        }
        val feriadosPublicos = holidays.filter { it.type == "Public" }
        for (holiday in feriadosUnicos) {

            val fechaFormateada = formatearFecha(holiday.date)

            val nombre = if (holiday.localName.isNotEmpty()) {
                holiday.localName
            } else {
                holiday.name
            }

            listaTexto.add("📅 $fechaFormateada - $nombre")
        }


        // Si la lista quedó vacía por alguna razón
        if (listaTexto.isEmpty()) {
            listaTexto.add("No hay feriados próximos")
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listaTexto
        )
        binding.lvFeriados.adapter = adapter

        // Hacer visible el ListView (inicia como GONE en el XML)
        binding.lvFeriados.visibility = View.VISIBLE

        // Mostrar total de feriados encontrados como Toast informativo
        Toast.makeText(
            requireContext(),
            "Se encontraron ${holidays.size} feriados",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Muestra un mensaje de error en tvErrorFeriados.
     * Se usa cuando la API no responde o no hay datos.
     *
     * @param mensaje Texto del error a mostrar
     */
    private fun mostrarErrorFeriados(mensaje: String) {
        binding.tvErrorFeriados.text = mensaje
        binding.tvErrorFeriados.visibility = View.VISIBLE
        binding.lvFeriados.visibility = View.GONE
    }

    /**
     * Convierte fecha de formato API "yyyy-MM-dd" a formato app "dd/MM/yyyy".
     * Ejemplo: "2026-07-28" → "28/07/2026"
     *
     * @param fechaApi Fecha en formato ISO de la API
     * @return Fecha formateada para mostrar al usuario
     */
    private fun formatearFecha(fechaApi: String): String {
        return try {
            val parts = fechaApi.split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) {
            fechaApi // Retornar original si falla el parseo
        }
    }

    /**
     * Compara si dos objetos Calendar representan el mismo día.
     * Se usa para incluir feriados del día actual en el filtro.
     *
     * @param cal1 Primer calendario
     * @param cal2 Segundo calendario
     * @return true si son el mismo día, false si no
     */
    private fun esMismoDia(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Recargar estadísticas al volver al fragment
    // No recarga feriados para evitar llamadas excesivas a la API
    override fun onResume() {
        super.onResume()
        cargarEstadisticas()
        cargarDoctoresTop()
    }

    // Limpiar binding al destruir la vista (evitar memory leaks)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}