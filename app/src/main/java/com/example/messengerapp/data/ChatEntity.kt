package com.example.messengerapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String,
    @ColumnInfo(name = "other_user_id") val otherUserId: String,
    @ColumnInfo(name = "last_message") val lastMessage: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long
){
    companion object {
        fun fromChatMessage(chat: Chat): ChatEntity {
            return ChatEntity(
                chatId = chat.chatId,
                otherUserId = chat.otherUserId,
                lastMessage = chat.lastMessage!!,
                timestamp =  chat.timestamp!!
            )
        }
    }
}
