package com.intoc.sistemacitasmedicas.api.response

import com.intoc.sistemacitasmedicas.bean.Paciente

/**
 * =====================================================
 * PacienteResponse.kt
 * =====================================================
 * Respuesta del servidor para operaciones de lectura
 * de pacientes (GET listar, GET buscar).
 *
 * Ejemplo JSON:
 * {
 *   "success": true,
 *   "message": "Pacientes encontrados",
 *   "data": [
 *     { "codigo": 1, "nombres": "Juan", ... },
 *     { "codigo": 2, "nombres": "María", ... }
 *   ]
 * }
 * =====================================================
 */
data class PacienteResponse(
    // Indica si la consulta fue exitosa
    val success: Boolean,
    // Mensaje descriptivo
    val message: String,
    // Lista de pacientes retornados por el servidor
    val data: List<Paciente>?
)