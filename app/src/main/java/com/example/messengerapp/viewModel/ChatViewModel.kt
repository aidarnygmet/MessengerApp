package com.example.messengerapp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.messengerapp.AppDatabase
import com.example.messengerapp.data.Chat
import com.example.messengerapp.data.ChatEntity
import com.example.messengerapp.data.Message
import com.example.messengerapp.data.MessageEntity
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "app-database"
    ).fallbackToDestructiveMigration()
        .build()
    lateinit var allChats: List<Chat>
    lateinit var allMessages: List<MessageEntity>
    init{
        viewModelScope.launch{
            allChats = getAllChats()
            allMessages = getAllMessages()
        }
    }

    suspend fun getAllMessages(): List<MessageEntity>{
        val messageEntities = database.messageDao().getAllMessages()
        return messageEntities
    }
    private suspend fun getAllChats(): List<Chat>{
        val chatEntities = database.chatDao().getAllChats()
        return convertChatEntitiesIntoChats(chatEntities)
    }
    private fun convertChatEntitiesIntoChats(chatEntities: List<ChatEntity>): List<Chat>{
        val chats = mutableListOf<Chat>()
        for(chatEntity in chatEntities){
            chats.add(Chat(chatEntity.chatId, chatEntity.otherUserId, chatEntity.lastMessage, chatEntity.timestamp))
        }
        return chats
    }
    private fun convertMessageEntitiesIntoMessages(messageEntities: List<MessageEntity>): List<Message>{
        val messages = mutableListOf<Message>()
        for(messageEntity in messageEntities){
            messages.add(Message(senderId = messageEntity.senderId, messageText = messageEntity.messageText, timestamp = messageEntity.timestamp))
        }
        return messages
    }
    suspend fun insertChat(chat: ChatEntity) {
        database.chatDao().insertChat(chat)
    }
    suspend fun insertMessage(message: MessageEntity) {
        database.messageDao().insertMessage(message)
    }
}
