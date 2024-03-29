package com.example.messengerapp.composables

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.data.Chat
import com.example.messengerapp.data.Message
import com.example.messengerapp.data.User
import com.example.messengerapp.viewModel.LoginViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatDetailScreen(navController: NavHostController,chat: Chat, loginViewModel: LoginViewModel) {
    val firebaseManager = FirebaseManager()
    val currentUser = loginViewModel.getCurrentUser()
    var otherUser by remember{ mutableStateOf(User("","","","","","","","")) }
    var isInputFocused by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(emptyList<Message>()) }
    val chats by loginViewModel.chats.collectAsState()
    if(chats.isNotEmpty()){
        val index = chats.indexOfFirst { it.chatId == chat.chatId }
        if(chats[index].lastMessage?.senderId != currentUser.userId){
            firebaseManager.nullifyUnreadCount(chat.chatId)
        }
    }
    LaunchedEffect(Unit) {
        firebaseManager.retrieveUserData(chat.otherUserId){
            otherUser = it
        }
        firebaseManager.retrieveMessages(chat.chatId) { retrievedMessages ->
            messages = retrievedMessages.reversed()
        }
    }
    Log.d("check", "ChatDetailScreen: ${chat.chatId}")

    if(otherUser.userId!=""){
        Scaffold(
            topBar = {
                ChatTopBar(otherUser, navController)
            },
            content = {
                ChatContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 56.dp, top = 56.dp),
                    messages = messages,
                    userId = currentUser.userId
                )
            },
            bottomBar = {
                ChatBottomBar(
                    onSendClicked = { toSent ->
                        firebaseManager.sendMessage(chat.chatId, Message(loginViewModel.getCurrentUser().userId, toSent, System.currentTimeMillis()))
                    },
                    onInputFocused = { isInputFocused = it }
                )
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
@Composable
fun ChatTopBar(user: User, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.clickable { navController.popBackStack() },
            tint = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(16.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatarRef.toUri())
                .build(),
            contentDescription = "",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = user.username, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}
@Composable
fun ChatContent(modifier: Modifier = Modifier, messages: List<Message> = emptyList(), userId: String) {
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        reverseLayout = true,
    ) {

        items(messages) { message ->
            ChatMessage(message = message, userId = userId)
            Spacer(modifier = Modifier.height(4.dp))
        }



    }
}
@Composable
fun ChatBottomBar(
    onSendClicked: (String) -> Unit,
    onInputFocused: (Boolean) -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->

        imageUri = uri
    }
    if(imageUri != null) {
        uploadImageMessage(imageUri!!, FirebaseStorage.getInstance().getReference("imageMessages")){ url ->
            Log.d("check", "calling onCallback")
            onSendClicked(url.toString())
        }
        imageUri = null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
        , horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    launcher.launch("image/*")
                }
                .padding(4.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))
        ChatMessageInput(
            onSendClicked = onSendClicked,
            onInputFocused = onInputFocused
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessageInput(
    onSendClicked: (String) -> Unit,
    onInputFocused: (Boolean) -> Unit

) {
    var messageText by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = messageText,
            onValueChange = { messageText = it },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.background)
                .onFocusChanged { onInputFocused(it.isFocused) },
            placeholder = { Text("Type a message...", modifier = Modifier.fillMaxSize(), style = MaterialTheme.typography.bodyLarge) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Send,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSendClicked(messageText)
                    messageText = ""
                }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Send,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clickable { onSendClicked(messageText)
                messageText = ""}
                .padding(4.dp),
                tint = MaterialTheme.colorScheme.primary
        )
    }
}
@Composable
fun ChatMessage(message: Message, userId: String) {
    Log.d("check", "ChatMessage: ${message.senderId} $userId")
    val alignment = if (message.senderId == userId) Arrangement.End else Arrangement.Start
    val bubbleColor = if (message.senderId == userId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.senderId == userId) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val timeAlignment = if (message.senderId == userId) Alignment.Start else Alignment.End
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = alignment
    ) {
        Card(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(bubbleColor)
                .padding(8.dp)

        ) {
            Column(
                modifier = Modifier
                    .background(bubbleColor)
                    .padding(4.dp)
                    .background(bubbleColor)
                    .clip(RectangleShape)
                    //.widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.6f)
            ) {
                if(isImageMessage(message.messageText)){
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(message.messageText)
                            .build(),
                        contentDescription = "",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop,
                    )
                }
                else{
                Text(text = message.messageText, color = textColor, style = MaterialTheme.typography.bodyLarge)}
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    modifier = Modifier.align(timeAlignment),
                    color = textColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun uploadImageMessage(imageUri: Uri, storageRef: StorageReference, onCallback: (Uri) -> Unit) {
    val filename = UUID.randomUUID().toString()
    val imagesRef = storageRef.child(filename)
    val uploadTask = imagesRef.putFile(imageUri)

    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                throw it
            }
        }
        imagesRef.downloadUrl
    }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                onCallback(downloadUri.toString().toUri())
                Log.d("check", "uploadTask: success: $downloadUri")
            } else {
                Log.d("check", "uploadTask: failed to get image url: $storageRef")
            }
        Log.d("check", "upload Successful")
    }
    uploadTask.addOnFailureListener { _ ->
        Log.d("check", "upload failed")
    }
}
fun isImageMessage(input: String): Boolean {
    val imageMessageAddress = "https://firebasestorage.googleapis.com/v0/b/messengerapp-3b5a1.appspot.com/o/imageMessages"
    return input.startsWith(imageMessageAddress)
}
