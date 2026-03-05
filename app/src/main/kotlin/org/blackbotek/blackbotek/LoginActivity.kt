package org.blackbotek.blackbotek

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val prefs = getSharedPreferences("BOT_CONFIG", Context.MODE_PRIVATE)
        val idUnico = prefs.getString("user_id", null)

        // Si ya está registrado, ir a MainActivity
        if (idUnico != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val checkGuardarSesion = findViewById<android.widget.CheckBox>(R.id.checkGuardarSesion)

        btnRegistrar.setOnClickListener {
            val telefono = findViewById<EditText>(R.id.etTelefono).text.toString()
            val contrasena = findViewById<EditText>(R.id.etContrasena).text.toString()

            if (telefono.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generar ID único
            val nuevoID = "ID-" + (1000..9999).random() + "-" + UUID.randomUUID().toString().substring(0, 4).uppercase()

            // Guardar en SharedPreferences
            prefs.edit().apply {
                putString("user_id", nuevoID)
                putString("telefono", telefono)
                putString("contrasena", contrasena)
                putBoolean("guardar_sesion", checkGuardarSesion.isChecked)
                apply()
            }

            // Guardar en Firebase
            val database = FirebaseDatabase.getInstance().getReference("Usuarios").child(nuevoID)
            val nuevosDatos = mapOf(
                "id" to nuevoID,
                "telefono" to telefono,
                "estado" to "pendiente",
                "dias_restantes" to 0,
                "fecha_inicio" to System.currentTimeMillis()
            )
            database.setValue(nuevosDatos)

            // Ir a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
