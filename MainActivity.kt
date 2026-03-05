package org.blackbotek.blackbotek

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private var idUnico: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Referencias de la interfaz
        val layoutBloqueo = findViewById<LinearLayout>(R.id.layoutBloqueo)
        val layoutBot = findViewById<LinearLayout>(R.id.layoutFuncionesBot)
        val txtID = findViewById<TextView>(R.id.txtIDUsuario)
        val btnPedirActivacion = findViewById<Button>(R.id.btnPedirActivacion)

        // Campos configurables
        val etPrecioKm = findViewById<EditText>(R.id.etPrecioKm)
        val etRadioRecogida = findViewById<EditText>(R.id.etRadioRecogida)
        val etCalifMin = findViewById<EditText>(R.id.etCalifMin)
        val etDelaySeguridad = findViewById<EditText>(R.id.etDelaySeguridad)
        val switchBotActivo = findViewById<Switch>(R.id.switchBotActivo)
        val btnGuardarConfig = findViewById<Button>(R.id.btnGuardarConfig)
        val txtEstadoBot = findViewById<TextView>(R.id.txtEstadoBot)

        // 2. Manejo del ID Único
        prefs = getSharedPreferences("BOT_CONFIG", Context.MODE_PRIVATE)
        idUnico = prefs.getString("user_id", null)

        if (idUnico == null) {
            idUnico = "BOT-" + (1000..9999).random() + "-" + UUID.randomUUID().toString().substring(0, 4).uppercase()
            prefs.edit().putString("user_id", idUnico).apply()
        }
        txtID.text = "ID: $idUnico"

        // 3. Cargar configuración guardada
        cargarConfiguracion(etPrecioKm, etRadioRecogida, etCalifMin, etDelaySeguridad, switchBotActivo)

        // 4. Botón WhatsApp
        btnPedirActivacion.setOnClickListener {
            val numeroAdmin = "5491122334455"
            val mensaje = "Hola, mi ID es: $idUnico. Solicito activación."
            val url = "https://wa.me/$numeroAdmin?text=${Uri.encode(mensaje)}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // 5. Botón Guardar Configuración
        btnGuardarConfig.setOnClickListener {
            guardarConfiguracion(
                etPrecioKm, etRadioRecogida, etCalifMin, etDelaySeguridad, switchBotActivo
            )
            Toast.makeText(this, "✓ Configuración guardada", Toast.LENGTH_SHORT).show()
        }

        // 6. Switch para activar/desactivar bot
        switchBotActivo.setOnCheckedChangeListener { _, isChecked ->
            txtEstadoBot.text = if (isChecked) "🟢 Bot ACTIVO" else "🔴 Bot INACTIVO"
            txtEstadoBot.setTextColor(if (isChecked) 0xFF00FF00.toInt() else 0xFFFF0000.toInt())
        }

        // 7. EL MOTOR DEL BOT (Validación en Firebase)
        val database = FirebaseDatabase.getInstance().getReference("Usuarios").child(idUnico!!)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estado = snapshot.child("estado").getValue(String::class.java) ?: "pendiente"
                val diasRestantes = snapshot.child("dias_restantes").getValue(Long::class.java) ?: 0L

                if (estado == "activo" && diasRestantes > 0) {
                    // ✅ TODO DESBLOQUEADO
                    layoutBloqueo.visibility = android.view.View.GONE
                    layoutBot.visibility = android.view.View.VISIBLE
                    
                    // El bot está activo y funcionando
                    configurarFiltrosYBot()
                } else {
                    // ❌ TODO BLOQUEADO
                    layoutBloqueo.visibility = android.view.View.VISIBLE
                    layoutBot.visibility = android.view.View.GONE
                    
                    if (!snapshot.exists()) {
                        database.setValue(mapOf(
                            "id" to idUnico,
                            "estado" to "pendiente",
                            "dias_restantes" to 0,
                            "telefono" to prefs.getString("telefono", ""),
                            "fecha_inicio" to System.currentTimeMillis()
                        ))
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Cargar configuración guardada
    private fun cargarConfiguracion(
        etPrecioKm: EditText,
        etRadioRecogida: EditText,
        etCalifMin: EditText,
        etDelaySeguridad: EditText,
        switchBotActivo: Switch
    ) {
        etPrecioKm.setText(prefs.getFloat("precio_km", 0f).toString())
        etRadioRecogida.setText(prefs.getFloat("radio_recogida", 999f).toString())
        etCalifMin.setText(prefs.getFloat("calif_min", 0f).toString())
        etDelaySeguridad.setText(prefs.getLong("delay_seguridad", 1000L).toString())
        switchBotActivo.isChecked = prefs.getBoolean("bot_activo", false)
    }

    // Guardar configuración
    private fun guardarConfiguracion(
        etPrecioKm: EditText,
        etRadioRecogida: EditText,
        etCalifMin: EditText,
        etDelaySeguridad: EditText,
        switchBotActivo: Switch
    ) {
        val editor = prefs.edit()
        editor.putFloat("precio_km", etPrecioKm.text.toString().toFloatOrNull() ?: 0f)
        editor.putFloat("radio_recogida", etRadioRecogida.text.toString().toFloatOrNull() ?: 999f)
        editor.putFloat("calif_min", etCalifMin.text.toString().toFloatOrNull() ?: 0f)
        editor.putLong("delay_seguridad", etDelaySeguridad.text.toString().toLongOrNull() ?: 1000L)
        editor.putBoolean("bot_activo", switchBotActivo.isChecked)
        editor.apply()
    }

    // Configurar filtros y bot
    private fun configurarFiltrosYBot() {
        val precioMin = prefs.getFloat("precio_km", 0f)
        val radioMax = prefs.getFloat("radio_recogida", 999f)
        val califMin = prefs.getFloat("calif_min", 0f)
        val delay = prefs.getLong("delay_seguridad", 1000L)
        
        // BotService.kt usará estos valores para funcionar
    }
}
