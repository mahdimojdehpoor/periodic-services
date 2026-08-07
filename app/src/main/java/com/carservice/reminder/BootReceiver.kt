package com.carservice.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            val items = db.serviceItemDao().getAllFuture(now)
            for (item in items) {
                val car = db.carDao().getById(item.carId)
                if (car != null) {
                    AlarmScheduler.schedule(context, item, car.name, car.id, car.socialLink)
                }
            }
        }
    }
}
