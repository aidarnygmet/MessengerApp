package com.example.messengerapp

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.example.messengerapp.composables.Navigation
import com.example.messengerapp.viewModel.LoginViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        val loginViewModel = LoginViewModel()
        val firebaseManager = FirebaseManager()
        if(!hasRequiredPermissions()){
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
        }
        if(!hasNotificationPermission()){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
        if (auth.currentUser != null) {
                firebaseManager.retrieveLinkToProfileId(auth.currentUser!!){
                    firebaseManager.retrieveUserData(it) {it1 ->
                        loginViewModel.setCurrentUser(it1)
                        loginViewModel.setProfilePictureUri(it1.avatarRef)
                    }
                }
        }
        setContent {
            if (!hasNotificationPermission()){
                Log.d("check", "onCreate: no notification permission")
            } else {
                showNotification()
                Log.d("check", "onCreate: has notification permission")
            }

            Navigation(auth, loginViewModel)
        }
    }
    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasNotificationPermission():Boolean{
        return ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
    companion object{
        private val CAMERAX_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    }

    fun showNotification(){
        val notification = NotificationCompat.Builder(applicationContext, "messengerApp")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hello world")
            .setContentText("This is a description")
            .build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}



















