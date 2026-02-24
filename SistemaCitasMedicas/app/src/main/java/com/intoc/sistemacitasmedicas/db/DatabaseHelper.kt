package com.intoc.sistemacitasmedicas.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.intoc.sistemacitasmedicas.bean.Cita
import com.intoc.sistemacitasmedicas.bean.Doctor
import com.intoc.sistemacitasmedicas.bean.Paciente

// Clase que gestiona la base de datos SQLite del sistema
// Implementa SQLiteOpenHelper para crear y actualizar la BD
// Sigue el patrón MVC como capa de Modelo
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Constantes de la base de datos
    companion object {
        private const val DATABASE_NAME = "MediCitasDB.db"  // Nombre del archivo de BD
        private const val DATABASE_VERSION = 1               // Versión de la BD

        // Tabla Pacientes
        private const val TABLA_PACIENTES = "pacientes"
        private const val COL_PAC_CODIGO = "codigo"
        private const val COL_PAC_NOMBRES = "nombres"
        private const val COL_PAC_APELLIDOS = "apellidos"
        private const val COL_PAC_DNI = "dni"
        private const val COL_PAC_TELEFONO = "telefono"
        private const val COL_PAC_CORREO = "correo"

        // Tabla Doctores
        private const val TABLA_DOCTORES = "doctores"
        private const val COL_DOC_CODIGO = "codigo"
        private const val COL_DOC_NOMBRES = "nombres"
        private const val COL_DOC_APELLIDOS = "apellidos"
        private const val COL_DOC_ESPECIALIDAD = "especialidad"
        private const val COL_DOC_TELEFONO = "telefono"
        private const val COL_DOC_COLEGIATURA = "colegiatura"

        private const val COL_DOC_CORREO = "correo"


        // Tabla Citas
        private const val TABLA_CITAS = "citas"
        private const val COL_CIT_CODIGO = "codigo"
        private const val COL_CIT_COD_PACIENTE = "codigoPaciente"
        private const val COL_CIT_COD_DOCTOR = "codigoDoctor"
        private const val COL_CIT_FECHA = "fecha"
        private const val COL_CIT_HORA = "hora"
        private const val COL_CIT_MOTIVO = "motivo"
        private const val COL_CIT_ESTADO = "estado"
    }

    // Se ejecuta al crear la base de datos por primera vez
    // Creando las tres tablas del sistema
    override fun onCreate(db: SQLiteDatabase?) {
        try {
            // Crear tabla de pacientes
            db?.execSQL(
                "CREATE TABLE $TABLA_PACIENTES (" +
                        "$COL_PAC_CODIGO INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$COL_PAC_NOMBRES TEXT, " +
                        "$COL_PAC_APELLIDOS TEXT, " +
                        "$COL_PAC_DNI TEXT, " +
                        "$COL_PAC_TELEFONO TEXT, " +
                        "$COL_PAC_CORREO TEXT)"
            )

            // Crear tabla de doctores
            db?.execSQL(
                "CREATE TABLE $TABLA_DOCTORES (" +
                        "$COL_DOC_CODIGO INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$COL_DOC_NOMBRES TEXT, " +
                        "$COL_DOC_APELLIDOS TEXT, " +
                        "$COL_DOC_ESPECIALIDAD TEXT, " +
                        "$COL_DOC_TELEFONO TEXT, " +
                        "$COL_DOC_COLEGIATURA TEXT, " +
                        "$COL_DOC_CORREO TEXT)"
            )

            // Crear tabla de citas
            db?.execSQL(
                "CREATE TABLE $TABLA_CITAS (" +
                        "$COL_CIT_CODIGO INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "$COL_CIT_COD_PACIENTE INTEGER, " +
                        "$COL_CIT_COD_DOCTOR INTEGER, " +
                        "$COL_CIT_FECHA TEXT, " +
                        "$COL_CIT_HORA TEXT, " +
                        "$COL_CIT_MOTIVO TEXT, " +
                        "$COL_CIT_ESTADO TEXT)"
            )

            // Insertar datos de ejemplo para demostración
            insertarDatosEjemplo(db)

            Log.d("DatabaseHelper", "Base de datos creada exitosamente")
        } catch (e: Exception) {
            Log.d("DatabaseHelper", "Error al crear BD: ${e.message}")
        }
    }

    // Se ejecuta cuando se actualiza la versión de la BD
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_PACIENTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_DOCTORES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_CITAS")
        onCreate(db)
    }

    // Inserta datos de ejemplo para que la app no esté vacía al iniciar
    private fun insertarDatosEjemplo(db: SQLiteDatabase?) {
        // Pacientes de ejemplo
        db?.execSQL("INSERT INTO $TABLA_PACIENTES (nombres, apellidos, dni, telefono, correo) VALUES ('Carlos', 'García López', '12345678', '987654321', 'carlos@mail.com')")
        db?.execSQL("INSERT INTO $TABLA_PACIENTES (nombres, apellidos, dni, telefono, correo) VALUES ('María', 'Torres Ruiz', '87654321', '912345678', 'maria@mail.com')")
        db?.execSQL("INSERT INTO $TABLA_PACIENTES (nombres, apellidos, dni, telefono, correo) VALUES ('Juan', 'Pérez Soto', '11223344', '956789012', 'juan@mail.com')")

        // Doctores de ejemplo
        db?.execSQL("INSERT INTO $TABLA_DOCTORES (nombres, apellidos, especialidad, telefono, colegiatura, correo) VALUES ('Roberto', 'Mendoza Díaz', 'Medicina General', '999111222', 'CMP-12345', 'roberto@mail.com')")
        db?.execSQL("INSERT INTO $TABLA_DOCTORES (nombres, apellidos, especialidad, telefono, colegiatura, correo) VALUES ('Ana', 'Vargas Flores', 'Pediatría', '999333444', 'CMP-67890', 'ana@mail.com')")
        db?.execSQL("INSERT INTO $TABLA_DOCTORES (nombres, apellidos, especialidad, telefono, colegiatura, correo) VALUES ('Luis', 'Ramírez Castro', 'Cardiología', '999555666', 'CMP-11111', 'luis@mail.com')")
        // Citas de ejemplo
        db?.execSQL("INSERT INTO $TABLA_CITAS (codigoPaciente, codigoDoctor, fecha, hora, motivo, estado) VALUES (1, 1, '25/02/2026', '09:00', 'Control general', 'Pendiente')")
        db?.execSQL("INSERT INTO $TABLA_CITAS (codigoPaciente, codigoDoctor, fecha, hora, motivo, estado) VALUES (2, 2, '26/02/2026', '10:30', 'Revisión pediátrica', 'Pendiente')")
    }

    // ===================== CRUD PACIENTES =====================

    // Agregar un nuevo paciente a la base de datos
    fun agregarPaciente(paciente: Paciente): Long {
        val db = this.writableDatabase
        val valores = ContentValues()
        valores.put(COL_PAC_NOMBRES, paciente.nombres)
        valores.put(COL_PAC_APELLIDOS, paciente.apellidos)
        valores.put(COL_PAC_DNI, paciente.dni)
        valores.put(COL_PAC_TELEFONO, paciente.telefono)
        valores.put(COL_PAC_CORREO, paciente.correo)
        val resultado = db.insert(TABLA_PACIENTES, null, valores)
        db.close()
        return resultado
    }

    // Actualizar los datos de un paciente existente
    fun actualizarPaciente(paciente: Paciente): Int {
        val db = this.writableDatabase
        val valores = ContentValues()
        valores.put(COL_PAC_NOMBRES, paciente.nombres)
        valores.put(COL_PAC_APELLIDOS, paciente.apellidos)
        valores.put(COL_PAC_DNI, paciente.dni)
        valores.put(COL_PAC_TELEFONO, paciente.telefono)
        valores.put(COL_PAC_CORREO, paciente.correo)
        val resultado = db.update(TABLA_PACIENTES, valores, "$COL_PAC_CODIGO=?", arrayOf(paciente.codigo.toString()))
        db.close()
        return resultado
    }

    // Eliminar un paciente por su código
    fun eliminarPaciente(codigo: Int): Int {
        val db = this.writableDatabase
        val resultado = db.delete(TABLA_PACIENTES, "$COL_PAC_CODIGO=?", arrayOf(codigo.toString()))
        db.close()
        return resultado
    }

    // Obtener todos los pacientes de la base de datos
    fun listarPacientes(): ArrayList<Paciente> {
        val lista = ArrayList<Paciente>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLA_PACIENTES", null)
        if (cursor.moveToFirst()) {
            do {
                val paciente = Paciente(
                    codigo = cursor.getInt(0),
                    nombres = cursor.getString(1),
                    apellidos = cursor.getString(2),
                    dni = cursor.getString(3),
                    telefono = cursor.getString(4),
                    correo = cursor.getString(5)
                )
                lista.add(paciente)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // Buscar pacientes por nombre (filtro de búsqueda)
    fun buscarPacientes(nombre: String): ArrayList<Paciente> {
        val lista = ArrayList<Paciente>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLA_PACIENTES WHERE $COL_PAC_NOMBRES LIKE ? OR $COL_PAC_APELLIDOS LIKE ?",
            arrayOf("%$nombre%", "%$nombre%")
        )
        if (cursor.moveToFirst()) {
            do {
                val paciente = Paciente(
                    codigo = cursor.getInt(0),
                    nombres = cursor.getString(1),
                    apellidos = cursor.getString(2),
                    dni = cursor.getString(3),
                    telefono = cursor.getString(4),
                    correo = cursor.getString(5)
                )
                lista.add(paciente)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // ===================== CRUD DOCTORES =====================

    // Agregar un nuevo doctor a la base de datos
    fun agregarDoctor(doctor: Doctor): Long {
        val db = this.writableDatabase
        val valores = ContentValues()
        valores.put(COL_DOC_NOMBRES, doctor.nombres)
        valores.put(COL_DOC_APELLIDOS, doctor.apellidos)
        valores.put(COL_DOC_ESPECIALIDAD, doctor.especialidad)
        valores.put(COL_DOC_TELEFONO, doctor.telefono)
        valores.put(COL_DOC_COLEGIATURA, doctor.colegiatura)
        valores.put("correo", doctor.correo)          // <-- AGREGAR
        val resultado = db.insert(TABLA_DOCTORES, null, valores)
        db.close()
        return resultado
    }

    // Actualizar los datos de un doctor existente
    fun actualizarDoctor(doctor: Doctor): Int {
        val db = this.writableDatabase
        val valores = ContentValues()
        valores.put(COL_DOC_NOMBRES, doctor.nombres)
        valores.put(COL_DOC_APELLIDOS, doctor.apellidos)
        valores.put(COL_DOC_ESPECIALIDAD, doctor.especialidad)
        valores.put(COL_DOC_TELEFONO, doctor.telefono)
        valores.put(COL_DOC_COLEGIATURA, doctor.colegiatura)
        valores.put("correo", doctor.correo)          // <-- AGREGAR
        val resultado = db.update(TABLA_DOCTORES, valores, "$COL_DOC_CODIGO=?", arrayOf(doctor.codigo.toString()))
        db.close()
        return resultado
    }
    // Eliminar un doctor por su código
    fun eliminarDoctor(codigo: Int): Int {
        val db = this.writableDatabase
        val resultado = db.delete(TABLA_DOCTORES, "$COL_DOC_CODIGO=?", arrayOf(codigo.toString()))
        db.close()
        return resultado
    }

    // Obtener todos los doctores de la base de datos
    fun listarDoctores(): ArrayList<Doctor> {
        val lista = ArrayList<Doctor>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLA_DOCTORES", null)
        if (cursor.moveToFirst()) {
            do {
                val doctor = Doctor(
                    codigo = cursor.getInt(0),
                    nombres = cursor.getString(1),
                    apellidos = cursor.getString(2),
                    especialidad = cursor.getString(3),
                    telefono = cursor.getString(4),
                    colegiatura = cursor.getString(5),
                    correo = cursor.getString(6)
                )
                lista.add(doctor)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // Buscar doctores por nombre
    fun buscarDoctores(nombre: String): ArrayList<Doctor> {
        val lista = ArrayList<Doctor>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLA_DOCTORES WHERE $COL_DOC_NOMBRES LIKE ? OR $COL_DOC_APELLIDOS LIKE ?",
            arrayOf("%$nombre%", "%$nombre%")
        )
        if (cursor.moveToFirst()) {
            do {
                val doctor = Doctor(
                    codigo = cursor.getInt(0),
                    nombres = cursor.getString(1),
                    apellidos = cursor.getString(2),
                    especialidad = cursor.getString(3),
                    telefono = cursor.getString(4),
                    colegiatura = cursor.getString(5),
                    correo = cursor.getString(6)   //
                )
                lista.add(doctor)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // ===================== CRUD CITAS =====================

    // Agregar una nueva cita médica
    fun agregarCita(cita: Cita): Long {
        val db = this.writableDatabase
        val valores = ContentValues()
        valores.put(COL_CIT_COD_PACIENTE, cita.codigoPaciente)
        valores.put(COL_CIT_COD_DOCTOR, cita.codigoDoctor)
        valores.put(COL_CIT_FECHA, cita.fecha)
        valores.put(COL_CIT_HORA, cita.hora)
        valores.put(COL_CIT_MOTIVO, cita.motivo)
        valores.put(COL_CIT_ESTADO, cita.estado)
        val resultado = db.insert(TABLA_CITAS, null, valores)
        db.close()
        return resultado
    }

    // Actualizar una cita existente
    fun actualizarCita(cita: Cita): Int {
        val db = this.writableDatabase
        val valores = ContentValues()
        valores.put(COL_CIT_COD_PACIENTE, cita.codigoPaciente)
        valores.put(COL_CIT_COD_DOCTOR, cita.codigoDoctor)
        valores.put(COL_CIT_FECHA, cita.fecha)
        valores.put(COL_CIT_HORA, cita.hora)
        valores.put(COL_CIT_MOTIVO, cita.motivo)
        valores.put(COL_CIT_ESTADO, cita.estado)
        val resultado = db.update(TABLA_CITAS, valores, "$COL_CIT_CODIGO=?", arrayOf(cita.codigo.toString()))
        db.close()
        return resultado
    }

    // Eliminar una cita por su código
    fun eliminarCita(codigo: Int): Int {
        val db = this.writableDatabase
        val resultado = db.delete(TABLA_CITAS, "$COL_CIT_CODIGO=?", arrayOf(codigo.toString()))
        db.close()
        return resultado
    }

    // Listar todas las citas con nombres de paciente y doctor
    fun listarCitas(): ArrayList<Cita> {
        val lista = ArrayList<Cita>()
        val db = this.readableDatabase
        // Consulta con JOIN para obtener nombres de paciente y doctor
        val query = """
            SELECT c.codigo, c.codigoPaciente, p.nombres || ' ' || p.apellidos AS nombrePaciente,
                   c.codigoDoctor, d.nombres || ' ' || d.apellidos AS nombreDoctor,
                   c.fecha, c.hora, c.motivo, c.estado
            FROM $TABLA_CITAS c
            INNER JOIN $TABLA_PACIENTES p ON c.codigoPaciente = p.codigo
            INNER JOIN $TABLA_DOCTORES d ON c.codigoDoctor = d.codigo
            ORDER BY c.fecha DESC
        """
        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val cita = Cita(
                    codigo = cursor.getInt(0),
                    codigoPaciente = cursor.getInt(1),
                    nombrePaciente = cursor.getString(2),
                    codigoDoctor = cursor.getInt(3),
                    nombreDoctor = cursor.getString(4),
                    fecha = cursor.getString(5),
                    hora = cursor.getString(6),
                    motivo = cursor.getString(7),
                    estado = cursor.getString(8)
                )
                lista.add(cita)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // Buscar citas por nombre de paciente
    fun buscarCitas(nombrePaciente: String): ArrayList<Cita> {
        val lista = ArrayList<Cita>()
        val db = this.readableDatabase
        val query = """
            SELECT c.codigo, c.codigoPaciente, p.nombres || ' ' || p.apellidos AS nombrePaciente,
                   c.codigoDoctor, d.nombres || ' ' || d.apellidos AS nombreDoctor,
                   c.fecha, c.hora, c.motivo, c.estado
            FROM $TABLA_CITAS c
            INNER JOIN $TABLA_PACIENTES p ON c.codigoPaciente = p.codigo
            INNER JOIN $TABLA_DOCTORES d ON c.codigoDoctor = d.codigo
            WHERE p.nombres LIKE ? OR p.apellidos LIKE ?
            ORDER BY c.fecha DESC
        """
        val cursor: Cursor = db.rawQuery(query, arrayOf("%$nombrePaciente%", "%$nombrePaciente%"))
        if (cursor.moveToFirst()) {
            do {
                val cita = Cita(
                    codigo = cursor.getInt(0),
                    codigoPaciente = cursor.getInt(1),
                    nombrePaciente = cursor.getString(2),
                    codigoDoctor = cursor.getInt(3),
                    nombreDoctor = cursor.getString(4),
                    fecha = cursor.getString(5),
                    hora = cursor.getString(6),
                    motivo = cursor.getString(7),
                    estado = cursor.getString(8)
                )
                lista.add(cita)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // ===================== REPORTES =====================

    // Contar total de pacientes registrados
    fun contarPacientes(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLA_PACIENTES", null)
        cursor.moveToFirst()
        val total = cursor.getInt(0)
        cursor.close()
        db.close()
        return total
    }

    // Contar total de doctores registrados
    fun contarDoctores(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLA_DOCTORES", null)
        cursor.moveToFirst()
        val total = cursor.getInt(0)
        cursor.close()
        db.close()
        return total
    }

    // Contar total de citas registradas
    fun contarCitas(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLA_CITAS", null)
        cursor.moveToFirst()
        val total = cursor.getInt(0)
        cursor.close()
        db.close()
        return total
    }

    // Contar citas por estado (Pendiente, Atendida, Cancelada)
    fun contarCitasPorEstado(estado: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLA_CITAS WHERE $COL_CIT_ESTADO = ?", arrayOf(estado))
        cursor.moveToFirst()
        val total = cursor.getInt(0)
        cursor.close()
        db.close()
        return total
    }
    // Obtener los doctores más solicitados (con más citas)
    // Retorna una lista de pares: "Dr. Nombre - Especialidad" → cantidad de citas
    fun doctoresMasSolicitados(): ArrayList<Pair<String, Int>> {
        val lista = ArrayList<Pair<String, Int>>()
        val db = this.readableDatabase
        val query = """
            SELECT d.nombres || ' ' || d.apellidos AS nombreDoctor, 
                   d.especialidad,
                   COUNT(c.codigo) AS totalCitas
            FROM $TABLA_CITAS c
            INNER JOIN $TABLA_DOCTORES d ON c.codigoDoctor = d.codigo
            GROUP BY c.codigoDoctor
            ORDER BY totalCitas DESC
            LIMIT 5
        """
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val nombre = "Dr. ${cursor.getString(0)} - ${cursor.getString(1)}"
                val total = cursor.getInt(2)
                lista.add(Pair(nombre, total))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }
    // ===== OBTENER PACIENTE POR CÓDIGO =====
    // Usado en DetalleCitaFragment para mostrar DNI y teléfono
    fun obtenerPacientePorCodigo(codigo: Int): Paciente? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLA_PACIENTES WHERE $COL_PAC_CODIGO = ?",
            arrayOf(codigo.toString())
        )
        var paciente: Paciente? = null
        if (cursor.moveToFirst()) {
            paciente = Paciente(
                codigo = cursor.getInt(0),
                nombres = cursor.getString(1),
                apellidos = cursor.getString(2),
                dni = cursor.getString(3),
                telefono = cursor.getString(4),
                correo = cursor.getString(5)    // <-- Cambiar direccion → correo
            )
        }
        cursor.close()
        db.close()
        return paciente
    }

    // ===== OBTENER DOCTOR POR CÓDIGO =====
    // Usado en DetalleCitaFragment para mostrar la especialidad
    fun obtenerDoctorPorCodigo(codigo: Int): Doctor? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLA_DOCTORES WHERE $COL_DOC_CODIGO = ?",
            arrayOf(codigo.toString())
        )
        var doctor: Doctor? = null
        if (cursor.moveToFirst()) {
            doctor = Doctor(
                codigo = cursor.getInt(0),
                nombres = cursor.getString(1),
                apellidos = cursor.getString(2),      // <-- era especialidad
                especialidad = cursor.getString(3),   // <-- era colegiatura
                telefono = cursor.getString(4),
                colegiatura = cursor.getString(5),
                correo = cursor.getString(6)          // <-- agregar
            )
        }
        cursor.close()
        db.close()
        return doctor
    }
}