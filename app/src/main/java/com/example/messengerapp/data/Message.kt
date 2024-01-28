package com.example.messengerapp.data

data class Message(
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0
)
{
    constructor() : this("", "", 0)
}