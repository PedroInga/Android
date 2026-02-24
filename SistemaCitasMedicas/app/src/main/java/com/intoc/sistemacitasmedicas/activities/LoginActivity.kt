package com.intoc.sistemacitasmedicas.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.intoc.sistemacitasmedicas.databinding.ActivityLoginBinding

// Activity de Login - única pantalla que NO usa Navigation Component
// Usa View Binding para acceder a las vistas sin findViewById
class LoginActivity : AppCompatActivity() {

    // View Binding: genera una clase a partir del XML del layout
    // Permite acceder a las vistas como propiedades (binding.edtUsuario)
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Evento botón INGRESAR
        binding.btnIngresar.setOnClickListener {
            val usuario = binding.edtUsuario.text.toString().trim()
            val contrasenia = binding.edtContrasenia.text.toString().trim()

            // Validar campos vacíos
            if (usuario.isEmpty()) {
                binding.edtUsuario.error = "Ingrese usuario"
                binding.edtUsuario.requestFocus()
                return@setOnClickListener
            }

            if (contrasenia.isEmpty()) {
                binding.edtContrasenia.error = "Ingrese contraseña"
                binding.edtContrasenia.requestFocus()
                return@setOnClickListener
            }

            // Validar credenciales locales
            if (usuario == "admin" && contrasenia == "1234") {
                Toast.makeText(this, "Bienvenido, $usuario", Toast.LENGTH_SHORT).show()
                // Navegar a MainActivity (que contiene el NavHostFragment)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("usuario", usuario)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }

        // Evento botón CANCELAR
        binding.btnCancelarLogin.setOnClickListener {
            binding.edtUsuario.setText("")
            binding.edtContrasenia.setText("")
            binding.edtUsuario.requestFocus()
        }
    }
}