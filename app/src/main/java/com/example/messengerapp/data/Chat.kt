package com.example.messengerapp.data

data class Chat(val chatId: String, val otherUserId: String, val lastMessage: Message?, val unreadCount: Int?)