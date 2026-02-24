package com.intoc.sistemacitasmedicas.api

import android.content.Context
import android.util.Log
import com.intoc.sistemacitasmedicas.api.response.*
import com.intoc.sistemacitasmedicas.bean.Cita
import com.intoc.sistemacitasmedicas.bean.Doctor
import com.intoc.sistemacitasmedicas.bean.Paciente
import com.intoc.sistemacitasmedicas.db.DatabaseHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * =====================================================
 * ApiManager.kt
 * =====================================================
 * Clase que gestiona TODAS las llamadas a APIs REST:
 *
 * 1) API MediCitas (Beeceptor) → CRUD con fallback a SQLite
 *    Si la API responde correctamente, usa datos de la API.
 *    Si falla (sin internet, servidor caído), usa BD local.
 *
 * 2) API Nager.Date (Feriados) → Consulta de feriados de Perú
 *    Se usa para validar fechas al registrar citas y
 *    mostrar próximos feriados en la sección de Reportes.
 *
 * Patrón: Manager / Repository
 * =====================================================
 */
class ApiManager(context: Context) {

    // Servicio API principal (MediCitas - Beeceptor)
    private val apiService = RetrofitClient.apiService

    // Servicio API de feriados (Nager.Date)
    private val holidayService = RetrofitClient.holidayApiService

    // Base de datos local SQLite (fallback)
    private val dbHelper = DatabaseHelper(context)

    // Tag para logs en Logcat
    companion object {
        private const val TAG = "ApiManager"
    }

    // ===================== PACIENTES =====================

    // Listar pacientes: intenta API, si falla usa SQLite
    fun listarPacientes(
        onSuccess: (ArrayList<Paciente>) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.listarPacientes().enqueue(object : Callback<PacienteResponse> {
            override fun onResponse(call: Call<PacienteResponse>, response: Response<PacienteResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val lista = ArrayList(response.body()?.data ?: emptyList())
                    Log.d(TAG, "Pacientes cargados desde API: ${lista.size}")
                    onSuccess(lista)
                } else {
                    Log.w(TAG, "API error, usando SQLite para pacientes")
                    onSuccess(dbHelper.listarPacientes())
                }
            }

            override fun onFailure(call: Call<PacienteResponse>, t: Throwable) {
                Log.e(TAG, "Sin conexión API: ${t.message}, usando SQLite")
                onSuccess(dbHelper.listarPacientes())
            }
        })
    }

    // Buscar pacientes por nombre
    fun buscarPacientes(
        nombre: String,
        onSuccess: (ArrayList<Paciente>) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.buscarPacientes(nombre).enqueue(object : Callback<PacienteResponse> {
            override fun onResponse(call: Call<PacienteResponse>, response: Response<PacienteResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess(ArrayList(response.body()?.data ?: emptyList()))
                } else {
                    onSuccess(dbHelper.buscarPacientes(nombre))
                }
            }

            override fun onFailure(call: Call<PacienteResponse>, t: Throwable) {
                onSuccess(dbHelper.buscarPacientes(nombre))
            }
        })
    }

    // Agregar paciente: envía a API y guarda en SQLite
    fun agregarPaciente(
        paciente: Paciente,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.agregarPaciente(paciente)

        apiService.agregarPaciente(paciente).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess("Paciente registrado (sincronizado con servidor)")
                } else {
                    onSuccess("Paciente registrado localmente")
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) {
                    onSuccess("Paciente registrado localmente (sin conexión)")
                } else {
                    onError("Error al registrar paciente")
                }
            }
        })
    }

    // Actualizar paciente
    fun actualizarPaciente(
        paciente: Paciente,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.actualizarPaciente(paciente)

        apiService.actualizarPaciente(paciente.codigo, paciente).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess("Paciente actualizado (sincronizado)")
                } else {
                    onSuccess("Paciente actualizado localmente")
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) {
                    onSuccess("Paciente actualizado localmente (sin conexión)")
                } else {
                    onError("Error al actualizar paciente")
                }
            }
        })
    }

    // Eliminar paciente
    fun eliminarPaciente(
        codigo: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.eliminarPaciente(codigo)

        apiService.eliminarPaciente(codigo).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                onSuccess("Paciente eliminado")
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) {
                    onSuccess("Paciente eliminado localmente")
                } else {
                    onError("Error al eliminar")
                }
            }
        })
    }

    // ===================== DOCTORES =====================

    // Listar doctores
    fun listarDoctores(
        onSuccess: (ArrayList<Doctor>) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.listarDoctores().enqueue(object : Callback<DoctorResponse> {
            override fun onResponse(call: Call<DoctorResponse>, response: Response<DoctorResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess(ArrayList(response.body()?.data ?: emptyList()))
                } else {
                    onSuccess(dbHelper.listarDoctores())
                }
            }

            override fun onFailure(call: Call<DoctorResponse>, t: Throwable) {
                onSuccess(dbHelper.listarDoctores())
            }
        })
    }

    // Buscar doctores
    fun buscarDoctores(
        nombre: String,
        onSuccess: (ArrayList<Doctor>) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.buscarDoctores(nombre).enqueue(object : Callback<DoctorResponse> {
            override fun onResponse(call: Call<DoctorResponse>, response: Response<DoctorResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess(ArrayList(response.body()?.data ?: emptyList()))
                } else {
                    onSuccess(dbHelper.buscarDoctores(nombre))
                }
            }

            override fun onFailure(call: Call<DoctorResponse>, t: Throwable) {
                onSuccess(dbHelper.buscarDoctores(nombre))
            }
        })
    }

    // Agregar doctor
    fun agregarDoctor(
        doctor: Doctor,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.agregarDoctor(doctor)

        apiService.agregarDoctor(doctor).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess("Doctor registrado (sincronizado)")
                } else {
                    onSuccess("Doctor registrado localmente")
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) {
                    onSuccess("Doctor registrado localmente (sin conexión)")
                } else {
                    onError("Error al registrar doctor")
                }
            }
        })
    }

    // Actualizar doctor
    fun actualizarDoctor(
        doctor: Doctor,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.actualizarDoctor(doctor)

        apiService.actualizarDoctor(doctor.codigo, doctor).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                onSuccess("Doctor actualizado")
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) onSuccess("Doctor actualizado localmente")
                else onError("Error al actualizar")
            }
        })
    }

    // Eliminar doctor
    fun eliminarDoctor(
        codigo: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.eliminarDoctor(codigo)

        apiService.eliminarDoctor(codigo).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                onSuccess("Doctor eliminado")
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) onSuccess("Doctor eliminado localmente")
                else onError("Error al eliminar")
            }
        })
    }

    // ===================== CITAS =====================

    // Listar citas
    fun listarCitas(
        onSuccess: (ArrayList<Cita>) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.listarCitas().enqueue(object : Callback<CitaResponse> {
            override fun onResponse(call: Call<CitaResponse>, response: Response<CitaResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess(ArrayList(response.body()?.data ?: emptyList()))
                } else {
                    onSuccess(dbHelper.listarCitas())
                }
            }

            override fun onFailure(call: Call<CitaResponse>, t: Throwable) {
                onSuccess(dbHelper.listarCitas())
            }
        })
    }

    // Buscar citas
    fun buscarCitas(
        nombrePaciente: String,
        onSuccess: (ArrayList<Cita>) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.buscarCitas(nombrePaciente).enqueue(object : Callback<CitaResponse> {
            override fun onResponse(call: Call<CitaResponse>, response: Response<CitaResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess(ArrayList(response.body()?.data ?: emptyList()))
                } else {
                    onSuccess(dbHelper.buscarCitas(nombrePaciente))
                }
            }

            override fun onFailure(call: Call<CitaResponse>, t: Throwable) {
                onSuccess(dbHelper.buscarCitas(nombrePaciente))
            }
        })
    }

    // Agregar cita
    fun agregarCita(
        cita: Cita,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.agregarCita(cita)

        apiService.agregarCita(cita).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                onSuccess("Cita registrada")
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) onSuccess("Cita registrada localmente")
                else onError("Error al registrar cita")
            }
        })
    }

    // Actualizar cita
    fun actualizarCita(
        cita: Cita,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.actualizarCita(cita)

        apiService.actualizarCita(cita.codigo, cita).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                onSuccess("Cita actualizada")
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) onSuccess("Cita actualizada localmente")
                else onError("Error al actualizar")
            }
        })
    }

    // Eliminar cita
    fun eliminarCita(
        codigo: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resultadoLocal = dbHelper.eliminarCita(codigo)

        apiService.eliminarCita(codigo).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                onSuccess("Cita eliminada")
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                if (resultadoLocal > 0) onSuccess("Cita eliminada localmente")
                else onError("Error al eliminar")
            }
        })
    }

    // ===================== REPORTES =====================

    // Obtener reportes
    fun obtenerReportes(
        onSuccess: (ReporteData) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.obtenerReportes().enqueue(object : Callback<ReporteResponse> {
            override fun onResponse(call: Call<ReporteResponse>, response: Response<ReporteResponse>) {
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    onSuccess(response.body()!!.data!!)
                } else {
                    onSuccess(construirReporteLocal())
                }
            }

            override fun onFailure(call: Call<ReporteResponse>, t: Throwable) {
                onSuccess(construirReporteLocal())
            }
        })
    }

    // Construye los datos de reporte desde SQLite cuando no hay API
    private fun construirReporteLocal(): ReporteData {
        val topDoctores = dbHelper.doctoresMasSolicitados().map { par ->
            DoctorTop(
                nombre = par.first,
                especialidad = "",
                totalCitas = par.second
            )
        }
        return ReporteData(
            totalPacientes = dbHelper.contarPacientes(),
            totalDoctores = dbHelper.contarDoctores(),
            totalCitas = dbHelper.contarCitas(),
            citasPendientes = dbHelper.contarCitasPorEstado("Pendiente"),
            citasConfirmadas = dbHelper.contarCitasPorEstado("Confirmada"),
            citasCompletadas = dbHelper.contarCitasPorEstado("Completada"),
            citasCanceladas = dbHelper.contarCitasPorEstado("Cancelada"),
            doctoresTop = topDoctores
        )
    }

    // =====================================================
    // ============ FERIADOS (API Nager.Date) ==============
    // =====================================================

    /**
     * Obtiene todos los feriados públicos de un año y país.
     * Usa la API pública Nager.Date (https://date.nager.at)
     *
     * @param year Año a consultar (ej: 2026)
     * @param countryCode Código del país (ej: "PE" para Perú)
     * @param onSuccess Callback con la lista de feriados
     * @param onError Callback con mensaje de error
     */
    fun getHolidays(
        year: Int,
        countryCode: String,
        onSuccess: (List<HolidayResponse>) -> Unit,
        onError: (String) -> Unit
    ) {
        // Usar el servicio de feriados (NO el apiService principal)
        val call = holidayService.getHolidays(year, countryCode)

        call.enqueue(object : Callback<List<HolidayResponse>> {
            override fun onResponse(
                call: Call<List<HolidayResponse>>,
                response: Response<List<HolidayResponse>>
            ) {
                if (response.isSuccessful) {
                    val holidays = response.body()
                    if (holidays != null) {
                        Log.d(TAG, "Feriados obtenidos desde API: ${holidays.size}")
                        onSuccess(holidays)
                    } else {
                        Log.w(TAG, "Respuesta vacía de feriados")
                        onError("No se encontraron feriados")
                    }
                } else {
                    Log.e(TAG, "Error HTTP feriados: ${response.code()}")
                    onError("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<HolidayResponse>>, t: Throwable) {
                Log.e(TAG, "Error conexión feriados: ${t.message}", t)
                onError("Error de conexión: ${t.message}")
            }
        })
    }

    /**
     * Obtiene los próximos feriados públicos de un país.
     *
     * @param countryCode Código del país (ej: "PE")
     * @param onSuccess Callback con la lista de próximos feriados
     * @param onError Callback con mensaje de error
     */
    fun getNextHolidays(
        countryCode: String,
        onSuccess: (List<HolidayResponse>) -> Unit,
        onError: (String) -> Unit
    ) {
        val call = holidayService.getNextHolidays(countryCode)

        call.enqueue(object : Callback<List<HolidayResponse>> {
            override fun onResponse(
                call: Call<List<HolidayResponse>>,
                response: Response<List<HolidayResponse>>
            ) {
                if (response.isSuccessful) {
                    val holidays = response.body()
                    if (holidays != null) {
                        Log.d(TAG, "Próximos feriados: ${holidays.size}")
                        onSuccess(holidays)
                    } else {
                        onError("No se encontraron próximos feriados")
                    }
                } else {
                    onError("Error del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<HolidayResponse>>, t: Throwable) {
                Log.e(TAG, "Error conexión próximos feriados: ${t.message}", t)
                onError("Error de conexión: ${t.message}")
            }
        })
    }

    /**
     * Verifica si una fecha específica es feriado en Perú.
     * Se usa al registrar una cita para advertir al usuario.
     *
     * @param fecha Fecha en formato "dd/MM/yyyy" (formato de la app)
     * @param year Año a consultar
     * @param onResult Callback con nombre del feriado (null si no es feriado)
     * @param onError Callback con mensaje de error
     */
    fun checkIfHoliday(
        fecha: String,
        year: Int,
        onResult: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        // Convertir fecha de "dd/MM/yyyy" a "yyyy-MM-dd" (formato de la API)
        val parts = fecha.split("/")
        if (parts.size != 3) {
            onResult(null) // Fecha inválida, no es feriado
            return
        }
        val fechaApi = "${parts[2]}-${parts[1]}-${parts[0]}" // yyyy-MM-dd

        // Consultar feriados del año usando la API Nager.Date
        getHolidays(year, "PE",
            onSuccess = { holidays ->
                // Buscar si la fecha coincide con algún feriado
                val feriado = holidays.find { it.date == fechaApi }
                if (feriado != null) {
                    Log.d(TAG, "¡Fecha es feriado!: ${feriado.localName}")
                    onResult(feriado.localName) // Retornar nombre del feriado
                } else {
                    onResult(null) // No es feriado
                }
            },
            onError = { error ->
                Log.e(TAG, "Error verificando feriado: $error")
                onError(error)
            }
        )
    }
}