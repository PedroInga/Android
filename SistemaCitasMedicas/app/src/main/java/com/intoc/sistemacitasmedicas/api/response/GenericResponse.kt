package com.intoc.sistemacitasmedicas.api.response

/**
 * =====================================================
 * GenericResponse.kt
 * =====================================================
 * Respuesta genérica del servidor para operaciones
 * de escritura (POST, PUT, DELETE).
 *
 * Ejemplo JSON:
 * {
 *   "success": true,
 *   "message": "Paciente registrado correctamente"
 * }
 * =====================================================
 */
data class GenericResponse(
    // Indica si la operación fue exitosa
    val success: Boolean,
    // Mensaje descriptivo del resultado
    val message: String
)