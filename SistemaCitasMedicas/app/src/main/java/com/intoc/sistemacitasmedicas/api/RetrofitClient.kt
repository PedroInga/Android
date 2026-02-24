package com.intoc.sistemacitasmedicas.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * =====================================================
 * RetrofitClient.kt
 * =====================================================
 * Singleton que configura y proporciona las instancias
 * de Retrofit para consumir las APIs REST.
 *
 * MANEJA 2 APIs DIFERENTES:
 *
 * 1) API MediCitas (Beeceptor):
 *    - Base URL: https://medicitas-api.free.beeceptor.com/api/
 *    - Uso: CRUD de Pacientes, Doctores, Citas, Reportes
 *    - Acceso: RetrofitClient.apiService
 *
 * 2) API Nager.Date (Feriados):
 *    - Base URL: https://date.nager.at/
 *    - Uso: Consultar feriados públicos de Perú
 *    - Acceso: RetrofitClient.holidayApiService
 *
 * Ambas comparten el mismo cliente HTTP con logging.
 * =====================================================
 */
object RetrofitClient {

    // ==================== URLs BASE ====================

    // URL de la API principal del sistema MediCitas (Beeceptor mock)
    // Para emulador Android Studio usar 10.0.2.2 (equivale a localhost)
    private const val BASE_URL_MEDICITAS = "https://medicitas-api.free.beeceptor.com/api/"

    // URL de la API pública de feriados (Nager.Date)
    // API gratuita, sin necesidad de API Key
    private const val BASE_URL_HOLIDAYS = "https://date.nager.at/"

    // ==================== CLIENTE HTTP ====================

    /**
     * Interceptor de logging para ver peticiones HTTP en Logcat.
     * Nivel BODY: muestra URL, headers, body de request y response.
     * Cambiar a Level.BASIC o Level.NONE en producción.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP compartido por ambas instancias de Retrofit.
     * Configurado con:
     * - Logging interceptor para depuración
     * - Timeouts de 30 segundos
     */
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ==================== RETROFIT MEDICITAS ====================

    /**
     * Instancia de Retrofit para la API principal MediCitas.
     * Maneja: Pacientes, Doctores, Citas, Reportes.
     */
    private val retrofitMediCitas: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_MEDICITAS)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Servicio API principal del sistema MediCitas.
     * Uso: RetrofitClient.apiService.listarPacientes()
     */
    val apiService: ApiService = retrofitMediCitas.create(ApiService::class.java)

    // ==================== RETROFIT FERIADOS ====================

    /**
     * Instancia de Retrofit para la API de feriados Nager.Date.
     * Usa 'lazy' para inicialización perezosa (se crea solo cuando se necesita).
     */
    private val retrofitHolidays: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_HOLIDAYS)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Servicio API de feriados públicos.
     * Uso: RetrofitClient.holidayApiService.getHolidays(2026, "PE")
     */
    val holidayApiService: HolidayApiService by lazy {
        retrofitHolidays.create(HolidayApiService::class.java)
    }
}