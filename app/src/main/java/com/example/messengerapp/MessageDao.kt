package com.example.messengerapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.messengerapp.data.ChatEntity
import com.example.messengerapp.data.MessageEntity

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages")
    suspend fun getAllMessages(): List<MessageEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}