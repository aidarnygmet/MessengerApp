package com.example.messengerapp.composables

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.data.Screen
import com.example.messengerapp.data.User
import com.example.messengerapp.viewModel.LoginViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatScreen(navController: NavController, loginViewModel: LoginViewModel) {
    var otherUserId by remember { mutableStateOf("") }
    val context = LocalContext.current
    Scaffold(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = otherUserId,
                    onValueChange = { otherUserId = it },
                    label = { Text(text = "Enter Other User ID") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            createChatAndNavigate(navController, otherUserId,loginViewModel.getCurrentUser())
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Button to create the chat
                Button(
                    onClick = {
                        // Handle button click
                        createChatAndNavigate(navController, otherUserId, loginViewModel.getCurrentUser())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Create Chat")
                }
            }
        }
    )
}



// Function to handle chat creation and navigation
private fun createChatAndNavigate(navController: NavController, otherUserId: String, user: User) {
    Log.d("check", "createChatAndNavigate: $otherUserId")
    val firebaseManager = FirebaseManager()
    firebaseManager.retrieveUserData(otherUserId){
        otherUser ->
        if(otherUser.userId == ""){
            Log.d("check", "createChatAndNavigate: User not found")
            return@retrieveUserData
        }
        firebaseManager.createChat(user, otherUser,{ chatId ->


            navController.navigate(Screen.ChatWithDetails.withArgs(chatId, otherUserId, otherUser.username))
        }, {
                e -> Log.d("check", "createChatAndNavigate: $e")
        })
    }
}
