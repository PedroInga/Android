package com.intoc.sistemacitasmedicas.bean

// Clase de datos (bean) que representa una Cita Médica
// Los campos nombrePaciente y nombreDoctor se obtienen del JOIN en la consulta SQL
data class Cita(
    var codigo: Int = 0,                // ID autoincremental de la cita
    var codigoPaciente: Int = 0,        // FK al paciente
    var nombrePaciente: String = "",    // Nombre completo del paciente (viene del JOIN)
    var codigoDoctor: Int = 0,          // FK al doctor
    var nombreDoctor: String = "",      // Nombre completo del doctor (viene del JOIN)
    var fecha: String = "",             // Fecha de la cita (dd/MM/yyyy)
    var hora: String = "",              // Hora de la cita (HH:mm)
    var motivo: String = "",            // Motivo de la consulta
    var estado: String = "Pendiente"    // Estado: Pendiente, Confirmada, Completada, Cancelada
)