package com.intoc.sistemacitasmedicas.api.response

import com.google.gson.annotations.SerializedName

/**
 * =====================================================
 * HolidayResponse.kt
 * =====================================================
 * Clase Response que mapea la respuesta JSON de la API
 * pública Nager.Date (https://date.nager.at)
 *
 * Esta API es DIFERENTE a la API REST del sistema MediCitas.
 * Se usa específicamente para consultar feriados públicos
 * de Perú y validar fechas al registrar citas.
 *
 * Ejemplo JSON de la API:
 * {
 *   "date": "2026-07-28",
 *   "localName": "Día de la Independencia",
 *   "name": "Independence Day",
 *   "countryCode": "PE",
 *   "fixed": true,
 *   "global": true,
 *   "counties": null,
 *   "launchYear": null,
 *   "types": ["Public"]
 * }
 * =====================================================
 */
data class HolidayResponse(
    // Fecha del feriado en formato "yyyy-MM-dd"
    @SerializedName("date")
    val date: String,

    // Nombre del feriado en idioma local (español para PE)
    @SerializedName("localName")
    val localName: String,

    // Nombre del feriado en inglés
    @SerializedName("name")
    val name: String,

    // Código del país (PE = Perú)
    @SerializedName("countryCode")
    val countryCode: String,

    // Indica si es fecha fija cada año
    @SerializedName("fixed")
    val fixed: Boolean,

    // Indica si aplica a todo el país
    @SerializedName("global")
    val global: Boolean,

    // Tipos de feriado (Public, Bank, School, etc.)
    @SerializedName("types")
    val types: List<String>?,
    val type: Any
)