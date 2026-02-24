package com.intoc.sistemacitasmedicas.api.response

import com.intoc.sistemacitasmedicas.bean.Cita

/**
 * =====================================================
 * CitaResponse.kt
 * =====================================================
 * Respuesta del servidor para operaciones de lectura
 * de citas médicas (GET listar, GET buscar).
 *
 * Ejemplo JSON:
 * {
 *   "success": true,
 *   "message": "Citas encontradas",
 *   "data": [
 *     { "codigo": 1, "codigoPaciente": 1, ... },
 *     { "codigo": 2, "codigoPaciente": 2, ... }
 *   ]
 * }
 * =====================================================
 */
data class CitaResponse(
    val success: Boolean,
    val message: String,
    val data: List<Cita>?
)