package com.kaquenduri.prueba01_mqtt.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kaquenduri.prueba01_mqtt.MainActivity
import com.kaquenduri.prueba01_mqtt.R


fun crearCanalNotificacion(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nombre = "Canal Alerta"
        val descripcion = "Notificaciones de alerta de sensores"
        val importancia = NotificationManager.IMPORTANCE_HIGH
        val canal = NotificationChannel("alerta_id", nombre, importancia).apply {
            this.description = descripcion
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
        }
        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }
}

fun mostrarNotificacion(context: Context, mensaje: String) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as MainActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }else{
            val builder = NotificationCompat.Builder(context, "alerta_id")
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Alerta de Humedad")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            Log.d("MainActivity", "Mostrando notificación: $mensaje")

            with(NotificationManagerCompat.from(context)) {
                notify(1, builder.build())
            }
        }
}