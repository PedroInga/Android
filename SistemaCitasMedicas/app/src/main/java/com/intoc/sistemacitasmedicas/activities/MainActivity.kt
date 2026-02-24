package com.intoc.sistemacitasmedicas.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.intoc.sistemacitasmedicas.R
import com.intoc.sistemacitasmedicas.databinding.ActivityMainBinding

// Activity principal que contiene el NavHostFragment
// Toda la navegación entre pantallas se maneja con Navigation Component
// Solo LoginActivity queda fuera del NavGraph
class MainActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityMainBinding

    // NavController: controla la navegación entre fragments
    lateinit var navController: NavController

    // Usuario logueado (recibido desde LoginActivity)
    var usuarioLogueado: String = "admin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar layout con View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el usuario del Intent
        usuarioLogueado = intent.getStringExtra("usuario") ?: "admin"

        // Configurar el NavController desde el NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    // Manejar el botón "atrás" del sistema con Navigation Component
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}