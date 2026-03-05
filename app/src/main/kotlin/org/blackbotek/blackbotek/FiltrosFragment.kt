package org.blackbotek.blackbotek

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FiltrosFragment : Fragment(), OnMapReadyCallback {

    private var mapBloquearZonas: MapView? = null
    private var mapRetornoACasa: MapView? = null
    private var googleMapBloquear: GoogleMap? = null
    private var googleMapRetorno: GoogleMap? = null

    private var radioLlegada = 2f
    private var multiplicadorHoraPico = 20f
    private var horaPicoActivo = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filtros, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("BOT_CONFIG", Context.MODE_PRIVATE)

        // Referencias
        mapBloquearZonas = view.findViewById(R.id.mapBloquearZonas)
        mapRetornoACasa = view.findViewById(R.id.mapRetornoACasa)
        val btnDibujarZona = view.findViewById<Button>(R.id.btnDibujarZona)
        val btnLimpiarZonas = view.findViewById<Button>(R.id.btnLimpiarZonas)
        val btnConfirmarPunto = view.findViewById<Button>(R.id.btnConfirmarPunto)
        val sliderRadioLlegada = view.findViewById<SeekBar>(R.id.sliderRadioLlegada)
        val txtRadioLlegada = view.findViewById<TextView>(R.id.txtRadioLlegada)
        val switchHoraPico = view.findViewById<Switch>(R.id.switchHoraPico)
        val sliderMultiplicador = view.findViewById<SeekBar>(R.id.sliderMultiplicador)
        val txtMultiplicador = view.findViewById<TextView>(R.id.txtMultiplicador)
        val btnGuardarFiltros = view.findViewById<Button>(R.id.btnGuardarFiltros)

        // Cargar valores guardados
        radioLlegada = prefs.getFloat("radio_llegada", 2f)
        multiplicadorHoraPico = prefs.getFloat("multiplicador_hora_pico", 20f)
        horaPicoActivo = prefs.getBoolean("hora_pico_activo", false)

        // Actualizar UI
        sliderRadioLlegada.progress = radioLlegada.toInt()
        txtRadioLlegada.text = String.format("%.1f KM", radioLlegada)
        switchHoraPico.isChecked = horaPicoActivo
        sliderMultiplicador.progress = multiplicadorHoraPico.toInt()
        txtMultiplicador.text = String.format("%d%%", multiplicadorHoraPico.toInt())

        // Inicializar MapViews
        mapBloquearZonas?.onCreate(savedInstanceState)
        mapBloquearZonas?.getMapAsync(this)

        mapRetornoACasa?.onCreate(savedInstanceState)
        mapRetornoACasa?.getMapAsync(this)

        // Botones Mapa Bloquear Zonas
        btnDibujarZona.setOnClickListener {
            Toast.makeText(requireContext(), "Modo dibujo activado", Toast.LENGTH_SHORT).show()
        }

        btnLimpiarZonas.setOnClickListener {
            googleMapBloquear?.clear()
            Toast.makeText(requireContext(), "Zonas bloqueadas eliminadas", Toast.LENGTH_SHORT).show()
        }

        // Botón Confirmar Punto (Retorno a Casa)
        btnConfirmarPunto.setOnClickListener {
            Toast.makeText(requireContext(), "✓ Punto de retorno confirmado", Toast.LENGTH_SHORT).show()
        }

        // Slider Radio Llegada
        sliderRadioLlegada.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radioLlegada = progress.toFloat()
                txtRadioLlegada.text = String.format("%.1f KM", radioLlegada)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Switch Modo Hora Pico
        switchHoraPico.setOnCheckedChangeListener { _, isChecked ->
            horaPicoActivo = isChecked
            sliderMultiplicador.isEnabled = isChecked
        }

        // Slider Multiplicador Hora Pico
        sliderMultiplicador.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                multiplicadorHoraPico = progress.toFloat()
                txtMultiplicador.text = String.format("%d%%", multiplicadorHoraPico.toInt())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Botón Guardar
        btnGuardarFiltros.setOnClickListener {
            val editor = prefs.edit()
            editor.putFloat("radio_llegada", radioLlegada)
            editor.putFloat("multiplicador_hora_pico", multiplicadorHoraPico)
            editor.putBoolean("hora_pico_activo", horaPicoActivo)
            editor.apply()

            Toast.makeText(requireContext(), "✓ Filtros guardados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Configurar mapas
        val ubicacionPorDefecto = LatLng(-34.9011, -56.1645) // Montevideo, Uruguay (ejemplo)

        if (googleMapBloquear == null) {
            googleMapBloquear = googleMap
            googleMapBloquear?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionPorDefecto, 12f))
        } else if (googleMapRetorno == null) {
            googleMapRetorno = googleMap
            googleMapRetorno?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionPorDefecto, 12f))
            googleMapRetorno?.addMarker(MarkerOptions().position(ubicacionPorDefecto).title("Mi Casa"))
        }
    }

    override fun onResume() {
        super.onResume()
        mapBloquearZonas?.onResume()
        mapRetornoACasa?.onResume()
    }

    override fun onPause() {
        mapBloquearZonas?.onPause()
        mapRetornoACasa?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapBloquearZonas?.onDestroy()
        mapRetornoACasa?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapBloquearZonas?.onLowMemory()
        mapRetornoACasa?.onLowMemory()
    }
}

