package com.example.messengerapp

import android.app.Service
import android.content.Intent
import android.os.IBinder

class NotificationService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}