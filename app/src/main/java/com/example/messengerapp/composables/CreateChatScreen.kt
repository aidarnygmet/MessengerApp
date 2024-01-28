package com.example.messengerapp.composables

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.R
import com.example.messengerapp.data.Chat
import com.example.messengerapp.data.ChatEntity
import com.example.messengerapp.data.Screen
import com.example.messengerapp.data.User
import com.example.messengerapp.viewModel.ChatViewModel
import com.example.messengerapp.viewModel.LoginViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateChatScreen(navController: NavController, firebaseManager: FirebaseManager, loginViewModel: LoginViewModel, chatViewModel: ChatViewModel) {
    var otherUserId by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
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
                            createChatAndNavigate(navController, otherUserId, firebaseManager, loginViewModel.getCurrentUser(), context, chatViewModel)
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
                        createChatAndNavigate(navController, otherUserId, firebaseManager, loginViewModel.getCurrentUser(),context, chatViewModel)
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
private fun createChatAndNavigate(navController: NavController, otherUserId: String, firebaseManager: FirebaseManager, user: User, context: Context, chatViewModel: ChatViewModel) {
    Log.d("check", "createChatAndNavigate: $otherUserId")
    firebaseManager.retrieveUserData(otherUserId){
        otherUser ->
        if(otherUser.userId == ""){
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
            Log.d("check", "createChatAndNavigate: User not found")
            return@retrieveUserData
        }
        firebaseManager.createChat(user, otherUser,{ chatId ->
            val chatEntity = ChatEntity.fromChatMessage(Chat(chatId, otherUserId, "", 0L))

            CoroutineScope(Dispatchers.Main). launch {
                chatViewModel.insertChat(chatEntity)
            }


            navController.navigate(Screen.ChatWithDetails.withArgs(chatId, otherUserId, otherUser.username))
        }, {
                e -> Log.d("check", "createChatAndNavigate: $e")
        })
    }

}
