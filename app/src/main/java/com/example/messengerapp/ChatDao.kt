package com.example.messengerapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.messengerapp.data.ChatEntity

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats")
    suspend fun getAllChats(): List<ChatEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
}
