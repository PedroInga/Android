package com.intoc.sistemacitasmedicas.api.response

/**
 * =====================================================
 * ReporteResponse.kt
 * =====================================================
 * Respuesta del servidor para el endpoint de reportes.
 * Contiene estadísticas generales del sistema.
 *
 * Ejemplo JSON:
 * {
 *   "success": true,
 *   "message": "Reportes generados",
 *   "data": {
 *     "totalPacientes": 15,
 *     "totalDoctores": 8,
 *     "totalCitas": 42,
 *     "citasPendientes": 10,
 *     "citasConfirmadas": 12,
 *     "citasCompletadas": 15,
 *     "citasCanceladas": 5,
 *     "doctoresTop": [...]
 *   }
 * }
 * =====================================================
 */
data class ReporteResponse(
    val success: Boolean,
    val message: String,
    val data: ReporteData?
)

/**
 * Datos internos del reporte con todas las estadísticas.
 */
data class ReporteData(
    val totalPacientes: Int,
    val totalDoctores: Int,
    val totalCitas: Int,
    val citasPendientes: Int,
    val citasConfirmadas: Int,
    val citasCompletadas: Int,
    val citasCanceladas: Int,
    val doctoresTop: List<DoctorTop>
)

/**
 * Representa un doctor en el ranking de más solicitados.
 */
data class DoctorTop(
    val nombre: String,
    val especialidad: String,
    val totalCitas: Int
)