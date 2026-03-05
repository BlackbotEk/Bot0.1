package org.blackbotek.blackbotek

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResumenFragment : Fragment() {

    private var botActivo = false
    private var diasRestantes = 0L
    private var idUnico: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_resumen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("BOT_CONFIG", Context.MODE_PRIVATE)
        idUnico = prefs.getString("user_id", null)

        // Referencias
        val txtEstadoBot = view.findViewById<TextView>(R.id.txtEstadoBot)
        val txtDiasRestantes = view.findViewById<TextView>(R.id.txtDiasRestantes)
        val txtEstadoSuscripcion = view.findViewById<TextView>(R.id.txtEstadoSuscripcion)
        val txtIDUsuario = view.findViewById<TextView>(R.id.txtIDUsuario)
        val txtVelocidadResumen = view.findViewById<TextView>(R.id.txtVelocidadResumen)
        val txtGananciaResumen = view.findViewById<TextView>(R.id.txtGananciaResumen)
        val txtPrecioOroResumen = view.findViewById<TextView>(R.id.txtPrecioOroResumen)
        val txtCalificacionResumen = view.findViewById<TextView>(R.id.txtCalificacionResumen)
        val txtHoraPicoResumen = view.findViewById<TextView>(R.id.txtHoraPicoResumen)
        val btnActivarBot = view.findViewById<Button>(R.id.btnActivarBot)
        val btnSolicitarActivacion = view.findViewById<Button>(R.id.btnSolicitarActivacion)

        // Mostrar ID
        txtIDUsuario.text = idUnico ?: "No disponible"

        // Cargar resumen de configuración
        cargarResumenConfiguracion(
            txtVelocidadResumen,
            txtGananciaResumen,
            txtPrecioOroResumen,
            txtCalificacionResumen,
            txtHoraPicoResumen,
            prefs
        )

        // Validar estado en Firebase
        if (idUnico != null) {
            val database = FirebaseDatabase.getInstance().getReference("Usuarios").child(idUnico!!)

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val estado = snapshot.child("estado").getValue(String::class.java) ?: "pendiente"
                    diasRestantes = snapshot.child("dias_restantes").getValue(Long::class.java) ?: 0L

                    botActivo = estado == "activo" && diasRestantes > 0

                    // Actualizar UI
                    if (botActivo) {
                        txtEstadoBot.text = "🟢 BOT ACTIVO"
                        txtEstadoBot.setTextColor(0xFF00FF00.toInt())
                        btnActivarBot.text = "🟢 DESACTIVAR BOT"
                        btnActivarBot.setBackgroundColor(0xFF4CAF50.toInt())
                    } else {
                        txtEstadoBot.text = "🔴 BOT INACTIVO"
                        txtEstadoBot.setTextColor(0xFFFF0000.toInt())
                        btnActivarBot.text = "🔴 ACTIVAR BOT"
                        btnActivarBot.setBackgroundColor(0xFFFF4444.toInt())
                    }

                    txtDiasRestantes.text = "$diasRestantes días"
                    txtEstadoSuscripcion.text = if (botActivo) "Activo" else "Pendiente"
                    txtEstadoSuscripcion.setTextColor(if (botActivo) 0xFF4CAF50.toInt() else 0xFFFF0000.toInt())
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error al cargar estado", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Botón Activar/Desactivar Bot
        btnActivarBot.setOnClickListener {
            if (botActivo) {
                // Desactivar
                val editor = prefs.edit()
                editor.putBoolean("bot_activo", false)
                editor.apply()
                Toast.makeText(requireContext(), "Bot desactivado", Toast.LENGTH_SHORT).show()
            } else {
                // Activar (solo si está validado en Firebase)
                if (diasRestantes > 0) {
                    val editor = prefs.edit()
                    editor.putBoolean("bot_activo", true)
                    editor.apply()
                    Toast.makeText(requireContext(), "Bot activado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Solicita activación al administrador", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Botón Solicitar Activación por WhatsApp
        btnSolicitarActivacion.setOnClickListener {
            val numeroAdmin = "5491122334455"
            val mensaje = "Hola, mi ID para el bot es: $idUnico. Solicito activación."
            val url = "https://wa.me/$numeroAdmin?text=${Uri.encode(mensaje)}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun cargarResumenConfiguracion(
        txtVelocidad: TextView,
        txtGanancia: TextView,
        txtPrecioOro: TextView,
        txtCalificacion: TextView,
        txtHoraPico: TextView,
        prefs: android.content.SharedPreferences
    ) {
        val velocidad = prefs.getLong("velocidad_reaccion", 350L)
        val ganancia = prefs.getFloat("ganancia_extra", 0f)
        val precioOroActivo = prefs.getBoolean("precio_oro_activo", false)
        val precioOroValor = prefs.getFloat("precio_oro_valor", 0f)
        val calificacion = prefs.getFloat("calif_min", 4.8f)
        val horaPicoActivo = prefs.getBoolean("hora_pico_activo", false)

        // Mostrar velocidad
        val velocidadTexto = when (velocidad) {
            50L -> "Flash (50ms)"
            150L -> "Competitivo (150ms)"
            350L -> "Normal (350ms)"
            600L -> "Humano (600ms)"
            else -> "Normal (350ms)"
        }
        txtVelocidad.text = velocidadTexto

        // Mostrar ganancia
        txtGanancia.text = String.format("$%.2f", ganancia)

        // Mostrar precio de oro
        txtPrecioOro.text = if (precioOroActivo) {
            String.format("Activo - $%.2f/KM", precioOroValor)
        } else {
            "Desactivado"
        }

        // Mostrar calificación
        txtCalificacion.text = String.format("%.1f ⭐", calificacion)

        // Mostrar hora pico
        txtHoraPico.text = if (horaPicoActivo) "Activo" else "Desactivado"
    }

    private fun startActivity(intent: Intent) {
        requireContext().startActivity(intent)
    }
}
