package org.blackbotek.blackbotek

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class VelocidadFragment : Fragment() {

    private var velocidadSeleccionada = 350L // Default: Normal Seguro
    private var gananciaExtra = 0f
    private var precioOroActivo = false
    private var precioOroValor = 0f
    private var calificacionMinima = 4.8f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_velocidad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("BOT_CONFIG", Context.MODE_PRIVATE)

        // Referencias
        val btnModoFlash = view.findViewById<Button>(R.id.btnModoFlash)
        val btnModoCompetitivo = view.findViewById<Button>(R.id.btnModoCompetitivo)
        val btnModoNormal = view.findViewById<Button>(R.id.btnModoNormal)
        val btnModoHumano = view.findViewById<Button>(R.id.btnModoHumano)
        val btnMenosGanancia = view.findViewById<Button>(R.id.btnMenosGanancia)
        val btnMasGanancia = view.findViewById<Button>(R.id.btnMasGanancia)
        val txtGanancia = view.findViewById<TextView>(R.id.txtGanancia)
        val switchPrecioOro = view.findViewById<Switch>(R.id.switchPrecioOro)
        val etPrecioOro = view.findViewById<EditText>(R.id.etPrecioOro)
        val sliderCalificacionPasajero = view.findViewById<SeekBar>(R.id.sliderCalificacionPasajero)
        val txtCalificacionPasajero = view.findViewById<TextView>(R.id.txtCalificacionPasajero)
        val btnGuardarVelocidad = view.findViewById<Button>(R.id.btnGuardarVelocidad)

        // Cargar valores guardados
        velocidadSeleccionada = prefs.getLong("velocidad_reaccion", 350L)
        gananciaExtra = prefs.getFloat("ganancia_extra", 0f)
        precioOroActivo = prefs.getBoolean("precio_oro_activo", false)
        precioOroValor = prefs.getFloat("precio_oro_valor", 0f)
        calificacionMinima = prefs.getFloat("calif_min", 4.8f)

        // Actualizar UI con valores guardados
        txtGanancia.text = String.format("$%.2f", gananciaExtra)
        switchPrecioOro.isChecked = precioOroActivo
        etPrecioOro.setText(precioOroValor.toString())
        sliderCalificacionPasajero.progress = (calificacionMinima * 10).toInt()
        txtCalificacionPasajero.text = String.format("%.1f", calificacionMinima)

        // Botones de Velocidad
        btnModoFlash.setOnClickListener {
            velocidadSeleccionada = 50L
            Toast.makeText(requireContext(), "Modo Flash (50ms) seleccionado", Toast.LENGTH_SHORT).show()
        }

        btnModoCompetitivo.setOnClickListener {
            velocidadSeleccionada = 150L
            Toast.makeText(requireContext(), "Modo Competitivo (150ms) seleccionado", Toast.LENGTH_SHORT).show()
        }

        btnModoNormal.setOnClickListener {
            velocidadSeleccionada = 350L
            Toast.makeText(requireContext(), "Modo Normal Seguro (350ms) seleccionado", Toast.LENGTH_SHORT).show()
        }

        btnModoHumano.setOnClickListener {
            velocidadSeleccionada = 600L
            Toast.makeText(requireContext(), "Modo Humano (600ms) seleccionado", Toast.LENGTH_SHORT).show()
        }

        // Botones Ganancia Extra
        btnMenosGanancia.setOnClickListener {
            gananciaExtra = (gananciaExtra - 5).coerceAtLeast(0f)
            txtGanancia.text = String.format("$%.2f", gananciaExtra)
        }

        btnMasGanancia.setOnClickListener {
            gananciaExtra += 5
            txtGanancia.text = String.format("$%.2f", gananciaExtra)
        }

        // Switch Precio de Oro
        switchPrecioOro.setOnCheckedChangeListener { _, isChecked ->
            precioOroActivo = isChecked
            etPrecioOro.isEnabled = isChecked
        }

        // Slider Calificación Pasajero
        sliderCalificacionPasajero.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                calificacionMinima = progress / 10f
                txtCalificacionPasajero.text = String.format("%.1f", calificacionMinima)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Botón Guardar
        btnGuardarVelocidad.setOnClickListener {
            precioOroValor = etPrecioOro.text.toString().toFloatOrNull() ?: 0f

            val editor = prefs.edit()
            editor.putLong("velocidad_reaccion", velocidadSeleccionada)
            editor.putFloat("ganancia_extra", gananciaExtra)
            editor.putBoolean("precio_oro_activo", precioOroActivo)
            editor.putFloat("precio_oro_valor", precioOroValor)
            editor.putFloat("calif_min", calificacionMinima)
            editor.apply()

            Toast.makeText(requireContext(), "✓ Configuración guardada", Toast.LENGTH_SHORT).show()
        }
    }
}
