package com.intoc.sistemacitasmedicas.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intoc.sistemacitasmedicas.R
import com.intoc.sistemacitasmedicas.activities.LoginActivity
import com.intoc.sistemacitasmedicas.activities.MainActivity
import com.intoc.sistemacitasmedicas.databinding.FragmentMenuPrincipalBinding

// Fragment del Menú Principal
// Usa View Binding y Navigation Component para navegar a cada sección
class MenuPrincipalFragment : Fragment() {

    // View Binding (nullable porque los fragments destruyen la vista)
    private var _binding: FragmentMenuPrincipalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar con View Binding
        _binding = FragmentMenuPrincipalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar nombre del usuario logueado
        val usuario = (activity as? MainActivity)?.usuarioLogueado ?: "admin"
        binding.tvUsuarioMenu.text = "Bienvenido, $usuario"

        // ===== NAVEGACIÓN CON NAVIGATION COMPONENT =====
        // Cada botón usa findNavController().navigate() con la acción del NavGraph

        // Botón PACIENTES → ListaPacientesFragment
        binding.cardPacientes.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_listaPacientes)
        }

        // Botón DOCTORES → ListaDoctoresFragment
        binding.cardDoctores.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_listaDoctores)
        }

        // Botón CITAS → ListaCitasFragment
        binding.cardCitas.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_listaCitas)
        }

        // Botón REPORTES → ReportesFragment
        binding.cardReportes.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_reportes)
        }

        // Botón UBICACIÓN → UbicacionFragment
        binding.cardUbicacion.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_ubicacion)
        }

        // Botón ACERCA DE → AcercaDeFragment
        binding.cardAcercaDe.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_acercaDe)
        }

        // Botón CERRAR SESIÓN → Volver a LoginActivity
        binding.btnCerrarSesion.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Está seguro de cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    // Limpiar binding al destruir la vista (evitar memory leaks)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}