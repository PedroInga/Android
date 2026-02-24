package com.intoc.sistemacitasmedicas.bean

// Clase de datos que representa un Paciente
// Se usa tanto para la BD local como para las respuestas de la API
data class Paciente(
    var codigo: Int = 0,
    var nombres: String = "",
    var apellidos: String = "",
    var dni: String = "",
    var telefono: String = "",
    var correo: String = ""
)