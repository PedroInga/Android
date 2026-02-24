package com.intoc.sistemacitasmedicas.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intoc.sistemacitasmedicas.databinding.FragmentAcercaDeBinding

// Fragment que muestra información sobre la aplicación
// Usa: View Binding, Navigation Component
// Pantalla informativa estática con datos del proyecto y desarrollador
class AcercaDeFragment : Fragment() {

    // View Binding (nullable para fragments)
    private var _binding: FragmentAcercaDeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar layout con View Binding
        _binding = FragmentAcercaDeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== EVENTO: Botón Volver al Menú =====
        // Usa Navigation Component para regresar al fragment anterior (Menú Principal)
        binding.btnVolverAcerca.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // Limpiar binding al destruir la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}