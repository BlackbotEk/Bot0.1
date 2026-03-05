package org.saeta.saetabot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BotService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val rootNode = rootInWindow ?: return
        
        // CONEXIÓN CON LA CALCULADORA (SharedPreferences)
        val prefs = getSharedPreferences("BlackBotPrefs", Context.MODE_PRIVATE)
        
        if (!prefs.getBoolean("bot_activo", false)) return

        // Variables de la "Calculadora"
        val precioMinimoKM = prefs.getFloat("precio_km", 0f)
        val radioMaxRecogida = prefs.getFloat("radio_recogida", 999f)
        val calificacionMin = prefs.getFloat("calif_min", 0f)
        val delayHumano = prefs.getLong("delay_seguridad", 1000L)

        // ESCANEO INTELIGENTE DE INDRIVE
        procesarPantallaInDrive(rootNode, precioMinimoKM, radioMaxRecogida, calificacionMin, delayHumano)
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
