package com.example.messengerapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity (

    @PrimaryKey(autoGenerate = true) var messageId: Long,

    @ColumnInfo(name = "chatId") val chatId: String,
    @ColumnInfo(name = "senderId") val senderId: String,
    @ColumnInfo(name = "messageText") val messageText: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)