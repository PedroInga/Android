package com.intoc.sistemacitasmedicas.api

import com.intoc.sistemacitasmedicas.api.response.*
import com.intoc.sistemacitasmedicas.bean.Cita
import com.intoc.sistemacitasmedicas.bean.Doctor
import com.intoc.sistemacitasmedicas.bean.Paciente
import retrofit2.Call
import retrofit2.http.*

/**
 * =====================================================
 * ApiService.kt
 * =====================================================
 * Interface que define todos los endpoints de la API REST.
 * Retrofit genera la implementación automáticamente.
 *
 * CONTIENE 2 GRUPOS DE ENDPOINTS:
 *
 * 1) API MediCitas (Beeceptor) → CRUD de Pacientes, Doctores, Citas, Reportes
 *    Base URL: https://medicitas-api.free.beeceptor.com/api/
 *
 * 2) API Nager.Date → Consulta de feriados públicos de Perú
 *    Base URL: https://date.nager.at/
 *
 * Cada grupo usa su propia instancia de Retrofit (ver RetrofitClient.kt)
 * =====================================================
 */
interface ApiService {

    // ===================== PACIENTES =====================

    // GET: Obtener todos los pacientes
    @GET("pacientes")
    fun listarPacientes(): Call<PacienteResponse>

    // GET: Buscar pacientes por nombre
    @GET("pacientes/buscar")
    fun buscarPacientes(@Query("nombre") nombre: String): Call<PacienteResponse>

    // POST: Agregar un nuevo paciente
    @POST("pacientes")
    fun agregarPaciente(@Body paciente: Paciente): Call<GenericResponse>

    // PUT: Actualizar un paciente existente
    @PUT("pacientes/{codigo}")
    fun actualizarPaciente(
        @Path("codigo") codigo: Int,
        @Body paciente: Paciente
    ): Call<GenericResponse>

    // DELETE: Eliminar un paciente
    @DELETE("pacientes/{codigo}")
    fun eliminarPaciente(@Path("codigo") codigo: Int): Call<GenericResponse>

    // ===================== DOCTORES =====================

    // GET: Obtener todos los doctores
    @GET("doctores")
    fun listarDoctores(): Call<DoctorResponse>

    // GET: Buscar doctores por nombre
    @GET("doctores/buscar")
    fun buscarDoctores(@Query("nombre") nombre: String): Call<DoctorResponse>

    // POST: Agregar un nuevo doctor
    @POST("doctores")
    fun agregarDoctor(@Body doctor: Doctor): Call<GenericResponse>

    // PUT: Actualizar un doctor existente
    @PUT("doctores/{codigo}")
    fun actualizarDoctor(
        @Path("codigo") codigo: Int,
        @Body doctor: Doctor
    ): Call<GenericResponse>

    // DELETE: Eliminar un doctor
    @DELETE("doctores/{codigo}")
    fun eliminarDoctor(@Path("codigo") codigo: Int): Call<GenericResponse>

    // ===================== CITAS =====================

    // GET: Obtener todas las citas
    @GET("citas")
    fun listarCitas(): Call<CitaResponse>

    // GET: Buscar citas por nombre de paciente
    @GET("citas/buscar")
    fun buscarCitas(@Query("paciente") nombrePaciente: String): Call<CitaResponse>

    // POST: Agregar una nueva cita
    @POST("citas")
    fun agregarCita(@Body cita: Cita): Call<GenericResponse>

    // PUT: Actualizar una cita existente
    @PUT("citas/{codigo}")
    fun actualizarCita(
        @Path("codigo") codigo: Int,
        @Body cita: Cita
    ): Call<GenericResponse>

    // DELETE: Eliminar una cita
    @DELETE("citas/{codigo}")
    fun eliminarCita(@Path("codigo") codigo: Int): Call<GenericResponse>

    // ===================== REPORTES =====================

    // GET: Obtener datos de reportes y estadísticas
    @GET("reportes")
    fun obtenerReportes(): Call<ReporteResponse>
}

/**
 * =====================================================
 * HolidayApiService
 * =====================================================
 * Interface SEPARADA para la API de feriados (Nager.Date).
 * Usa una base URL diferente a la API principal.
 *
 * Se accede mediante: RetrofitClient.holidayApiService
 * =====================================================
 */
interface HolidayApiService {

    /**
     * Obtiene todos los feriados públicos de un año y país.
     *
     * @param year Año a consultar (ej: 2026)
     * @param countryCode Código ISO del país (ej: "PE" para Perú)
     * @return Lista de HolidayResponse con todos los feriados
     *
     * URL generada: https://date.nager.at/api/v3/PublicHolidays/2026/PE
     */
    @GET("api/v3/PublicHolidays/{year}/{countryCode}")
    fun getHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): Call<List<HolidayResponse>>

    /**
     * Obtiene los próximos feriados públicos de un país.
     *
     * @param countryCode Código ISO del país (ej: "PE")
     * @return Lista de próximos feriados
     *
     * URL generada: https://date.nager.at/api/v3/NextPublicHolidays/PE
     */
    @GET("api/v3/NextPublicHolidays/{countryCode}")
    fun getNextHolidays(
        @Path("countryCode") countryCode: String
    ): Call<List<HolidayResponse>>
}