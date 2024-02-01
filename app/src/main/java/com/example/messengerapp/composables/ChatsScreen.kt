package com.example.messengerapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.R
import com.example.messengerapp.data.Chat
import com.example.messengerapp.data.Screen
import com.example.messengerapp.data.User
import com.example.messengerapp.viewModel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsScreen(navController: NavHostController, firebaseManager: FirebaseManager, loginViewModel: LoginViewModel) {
    //var chats by remember { mutableStateOf(loginViewModel.chats) }
    val chats by loginViewModel.chats.collectAsState()
    LaunchedEffect(Unit) {
//        loginViewModel.currentUser.value?.let {
//            firebaseManager.retrieveChats(it) { retrievedChats ->
//                chats = retrievedChats.sortedByDescending { it1 -> it1.timestamp }
//                loginViewModel.setChats()
//            }
//        }
        loginViewModel.setChats()
    }
    if(chats.isNotEmpty()){
        Column {
            SearchBar()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                items(chats) { chat ->
                    ChatItem(viewModel = loginViewModel,navController, chat, firebaseManager)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "No chats yet\nStart chatting by pressing Plus button", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ChatItem(viewModel: LoginViewModel, navController: NavHostController, chat: Chat, firebaseManager: FirebaseManager) {
    var otherUser by remember { mutableStateOf(User("","","", "", "", "","", "")) }
    val currentUser = viewModel.getCurrentUser()

    LaunchedEffect(Unit){
        if(chat.otherUserId != currentUser.userId){
            firebaseManager.retrieveUserData(chat.otherUserId){
                otherUser = it
            }

        } else {
            val words = chat.chatId.split("_")
            var otherUserId = ""
            if (currentUser.userId == words[0]) {
                otherUserId = words[1]
            } else if (currentUser.userId == words[1]) {
                otherUserId = words[0]
            }
            firebaseManager.retrieveUserData(otherUserId){
                otherUser = it
            }
        }
    }
    if(otherUser.username !=""){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .clickable {

                    navController.navigate(
                        Screen.ChatWithDetails.withArgs(
                            chat.chatId
                        )
                    )

                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if(otherUser.avatarRef == ""){
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(otherUser.avatarRef.toUri())
                        .build(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                ,verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = otherUser.username, fontWeight = FontWeight.Bold)
                    if(isImageMessage(chat.lastMessage!!.messageText)){
                        Text(text = "Image", maxLines = 1, color = Color.Gray)
                    } else {
                        Text(text = chat.lastMessage.messageText, maxLines = 1, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(text = sdf.format(Date(chat.lastMessage!!.timestamp)), fontWeight = FontWeight.Bold)
                    Spacer(modifier =Modifier.height(4.dp))
                    if(chat.lastMessage.senderId != currentUser.userId && chat.unreadCount != 0){
                        Text(
                            text = chat.unreadCount.toString(), fontWeight = FontWeight.Bold, modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                .clip(CircleShape),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}



