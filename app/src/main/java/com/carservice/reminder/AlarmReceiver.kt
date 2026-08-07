package com.carservice.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceName = intent.getStringExtra("serviceName") ?: ""
        val carName = intent.getStringExtra("carName") ?: ""
        val carId = intent.getIntExtra("carId", -1)
        val socialLink = intent.getStringExtra("socialLink")

        val targetIntent = if (!socialLink.isNullOrBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(socialLink))
        } else {
            Intent(context, CarDetailActivity::class.java).apply {
                putExtra("carId", carId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            carId,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, App.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("یادآوری سرویس: $serviceName")
            .setContentText("زمان سرویس $serviceName برای $carName رسیده است")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(carId * 1000 + serviceName.hashCode(), notification)
        } catch (e: SecurityException) {
            // اجازه نوتیفیکیشن داده نشده
        }
    }
}
