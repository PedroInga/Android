package com.intoc.sistemacitasmedicas.bean

data class Doctor(
    var codigo: Int = 0,
    var nombres: String = "",
    var apellidos: String = "",
    var especialidad: String = "",
    var telefono: String = "",
    var colegiatura: String = "",
    var correo: String = ""
)