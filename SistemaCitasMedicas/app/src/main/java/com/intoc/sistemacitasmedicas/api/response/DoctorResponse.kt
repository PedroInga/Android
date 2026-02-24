package com.intoc.sistemacitasmedicas.api.response

import com.intoc.sistemacitasmedicas.bean.Doctor

/**
 * =====================================================
 * DoctorResponse.kt
 * =====================================================
 * Respuesta del servidor para operaciones de lectura
 * de doctores (GET listar, GET buscar).
 *
 * Ejemplo JSON:
 * {
 *   "success": true,
 *   "message": "Doctores encontrados",
 *   "data": [
 *     { "codigo": 1, "nombres": "Carlos", ... },
 *     { "codigo": 2, "nombres": "Ana", ... }
 *   ]
 * }
 * =====================================================
 */
data class DoctorResponse(
    val success: Boolean,
    val message: String,
    val data: List<Doctor>?
)