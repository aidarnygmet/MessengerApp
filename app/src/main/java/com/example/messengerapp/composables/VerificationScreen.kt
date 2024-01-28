package com.example.messengerapp.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.data.Screen
import com.example.messengerapp.viewModel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VerificationScreen(navController:NavHostController ,firebaseManager: FirebaseManager, loginViewModel: LoginViewModel, auth: FirebaseAuth, email: String, password: String, username: String){
    Column {
        Text(text ="Please, verify your email and click the button below")
        Button(onClick = {
            loginViewModel.signIn(firebaseManager, auth, email, password, username){
                navController.navigate(Screen.Chats.route)
            }
        }) {
            Text(text = "I have verified my email")
        }
    }
}