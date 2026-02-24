package com.intoc.sistemacitasmedicas.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.intoc.sistemacitasmedicas.R
import com.intoc.sistemacitasmedicas.databinding.FragmentUbicacionBinding

// Fragment que muestra la ubicación de la clínica en Google Maps
// Usa: View Binding, Google Maps API, SupportMapFragment,
// Intent implícito para abrir Google Maps con navegación
// Implementa OnMapReadyCallback para recibir el mapa cuando esté listo
class UbicacionFragment : Fragment(), OnMapReadyCallback {

    // View Binding (nullable para fragments)
    private var _binding: FragmentUbicacionBinding? = null
    private val binding get() = _binding!!

    // Referencia al mapa de Google
    private var googleMap: GoogleMap? = null

    // Coordenadas de la clínica (Av. La Marina 2500, San Miguel, Lima)
    // Latitud y longitud aproximadas de la ubicación
    private val latClinica = -12.0763
    private val lngClinica = -77.0950

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar layout con View Binding
        _binding = FragmentUbicacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== INICIALIZAR GOOGLE MAPS =====
        // Obtener el SupportMapFragment del layout usando su ID
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        // Solicitar el mapa de forma asíncrona
        // Cuando esté listo, se ejecutará onMapReady()
        mapFragment.getMapAsync(this)

        // ===== EVENTO: Botón "¿Cómo llegar?" =====
        // Abre Google Maps con navegación hacia la clínica
        binding.btnComoLlegar.setOnClickListener {
            abrirGoogleMaps()
        }
    }

    // ===== CALLBACK: Mapa listo para usar =====
    // Se ejecuta cuando Google Maps ha terminado de cargar
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Crear objeto LatLng con las coordenadas de la clínica
        val ubicacionClinica = LatLng(latClinica, lngClinica)

        // Agregar marcador en la ubicación de la clínica
        googleMap?.addMarker(
            MarkerOptions()
                .position(ubicacionClinica)                    // Posición del marcador
                .title("Clínica MediCitas")                    // Título del marcador
                .snippet("Av. La Marina 2500, San Miguel")     // Subtítulo del marcador
        )

        // Mover la cámara a la ubicación de la clínica con zoom 16
        // Zoom 16 = nivel de calle, ideal para ver la ubicación exacta
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(ubicacionClinica, 16f)
        )

        // Habilitar controles de zoom en el mapa
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        // Configurar tipo de mapa (Normal = calles estándar)
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    // ===== ABRIR GOOGLE MAPS CON NAVEGACIÓN =====
    // Usa un Intent implícito para abrir Google Maps
    // con las coordenadas de la clínica y solicitar navegación
    private fun abrirGoogleMaps() {
        try {
            // URI de Google Maps con coordenadas y etiqueta
            // Formato: geo:lat,lng?q=lat,lng(Etiqueta)
            val uri = Uri.parse("google.navigation:q=$latClinica,$lngClinica")

            // Crear Intent implícito para abrir Google Maps
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")  // Especificar Google Maps

            // Verificar que Google Maps esté instalado
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                // Si no está instalado, abrir en el navegador
                val webUri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination=$latClinica,$lngClinica"
                )
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error al abrir Google Maps: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Limpiar binding al destruir la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}