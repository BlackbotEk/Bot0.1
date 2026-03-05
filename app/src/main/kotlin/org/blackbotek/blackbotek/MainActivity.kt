package org.blackbotek.blackbotek

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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

        // Inicializar SharedPreferences
        prefs = getSharedPreferences("BOT_CONFIG", Context.MODE_PRIVATE)
        idUnico = prefs.getString("user_id", null)

        // Si no existe ID, generar uno
        if (idUnico == null) {
            idUnico = "BOT-" + (1000..9999).random() + "-" + UUID.randomUUID().toString().substring(0, 4).uppercase()
            prefs.edit().putString("user_id", idUnico).apply()
        }

        // Configurar TabLayout y ViewPager2
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // Crear adaptador
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Conectar TabLayout con ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "VELOCIDAD"
                1 -> "FILTROS"
                2 -> "RESUMEN"
                else -> ""
            }
        }.attach()

        // Validar estado en Firebase
        validarEstadoEnFirebase()
    }

    private fun validarEstadoEnFirebase() {
        if (idUnico == null) return

        val database = FirebaseDatabase.getInstance().getReference("Usuarios").child(idUnico!!)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estado = snapshot.child("estado").getValue(String::class.java) ?: "pendiente"
                val diasRestantes = snapshot.child("dias_restantes").getValue(Long::class.java) ?: 0L

                if (estado == "activo" && diasRestantes > 0) {
                    // Bot está activo
                    prefs.edit().putBoolean("bot_activo", true).apply()
                } else {
                    // Bot no está activo
                    prefs.edit().putBoolean("bot_activo", false).apply()

                    // Si no existe en Firebase, crear registro
                    if (!snapshot.exists()) {
                        val nuevosDatos = mapOf(
                            "id" to idUnico,
                            "estado" to "pendiente",
                            "dias_restantes" to 0,
                            "telefono" to prefs.getString("telefono", ""),
                            "fecha_inicio" to System.currentTimeMillis()
                        )
                        database.setValue(nuevosDatos)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al conectar con Firebase", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
