package org.blackbotek.blackbotek

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BotService : AccessibilityService() {

    private var botActivo = false
    private var diasRestantes = 0L
    private var idUnico: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val rootNode = rootInWindow ?: return
        
        // CONEXIÓN CON FIREBASE
        val prefs = getSharedPreferences("BOT_CONFIG", Context.MODE_PRIVATE)
        idUnico = prefs.getString("user_id", null) ?: return

        // Validar en Firebase si el bot está activo
        validarEstadoEnFirebase(idUnico!!) { activo, dias ->
            botActivo = activo
            diasRestantes = dias

            if (!botActivo || diasRestantes <= 0) return@validarEstadoEnFirebase

            // Variables de configuración
            val precioMinimoKM = prefs.getFloat("precio_km", 0f)
            val radioMaxRecogida = prefs.getFloat("radio_recogida", 999f)
            val calificacionMin = prefs.getFloat("calif_min", 0f)
            val delayHumano = prefs.getLong("delay_seguridad", 1000L)

            // ESCANEO INTELIGENTE DE INDRIVE
            procesarPantallaInDrive(rootNode, precioMinimoKM, radioMaxRecogida, calificacionMin, delayHumano)
        }
    }

    // Validar estado en Firebase
    private fun validarEstadoEnFirebase(idUnico: String, callback: (Boolean, Long) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("Usuarios").child(idUnico)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estado = snapshot.child("estado").getValue(String::class.java) ?: "pendiente"
                val dias = snapshot.child("dias_restantes").getValue(Long::class.java) ?: 0L

                callback(estado == "activo", dias)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, 0L)
            }
        })
    }

    private fun procesarPantallaInDrive(
        node: AccessibilityNodeInfo,
        pKm: Float,
        rMax: Float,
        cMin: Float,
        delay: Long
    ) {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val texto = child.text?.toString() ?: ""

            if (esViajeRentable(texto, pKm, rMax, cMin)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    ejecutarToqueHumano(child)
                }, delay + (100..500).random())
                return
            }
            procesarPantallaInDrive(child, pKm, rMax, cMin, delay)
        }
    }

    private fun esViajeRentable(texto: String, pKm: Float, rMax: Float, cMin: Float): Boolean {
        // Lógica de filtrado: extrae precio, distancia y calificación
        // Retorna true si el viaje cumple los criterios
        return texto.contains("Aceptar")
    }

    private fun ejecutarToqueHumano(node: AccessibilityNodeInfo) {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        
        val x = (400..600).random().toFloat()
        val y = (800..1000).random().toFloat()
        
        path.moveTo(x, y)
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    override fun onInterrupt() {}
}
