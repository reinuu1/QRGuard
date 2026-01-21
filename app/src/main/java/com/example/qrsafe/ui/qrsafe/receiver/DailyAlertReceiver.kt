package com.example.qrsafe.ui.qrsafe.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.qrsafe.ui.qrsafe.MainActivity
import com.example.qrsafe.R

class DailyAlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DailyAlertReceiver", "ALARMA A SUNAT! Încerc să trimit notificare...")
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "cyber_academy_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cyber Academy Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificări de securitate"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Folosim iconita default a aplicatiei
            .setContentTitle("⚠️ Alertă de Securitate!")
            .setContentText("Cineva a încercat să scaneze un cod fals. Verifică acum!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
        Log.d("DailyAlertReceiver", "Notificarea a fost trimisă către sistem.")
    }
}