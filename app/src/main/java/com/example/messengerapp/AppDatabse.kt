package com.example.messengerapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.messengerapp.data.ChatEntity
import com.example.messengerapp.data.MessageEntity

@Database(entities = [ChatEntity::class, MessageEntity::class], version = 2)
abstract class AppDatabase(): RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}