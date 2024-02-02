package com.example.messengerapp

// FirebaseManager.kt
import android.util.Log
import com.example.messengerapp.data.Chat
import com.example.messengerapp.data.Message
import com.example.messengerapp.data.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseManager {

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val chatsRef = database.getReference("chats")

    fun retrieveUserData(userId: String, callback: (User) -> Unit) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("name").getValue(String::class.java).orEmpty()
                val avatarRef = snapshot.child("avatarRef").getValue(String::class.java).orEmpty()
                val bio = snapshot.child("bio").getValue(String::class.java).orEmpty()
                val linkToInstagram = snapshot.child("linkToInstagram").getValue(String::class.java).orEmpty()
                val linkToFacebook = snapshot.child("linkToFacebook").getValue(String::class.java).orEmpty()
                val linkToTwitter = snapshot.child("linkToTwitter").getValue(String::class.java).orEmpty()
                val linkToLinkedIn = snapshot.child("linkedIn").getValue(String::class.java).orEmpty()
                Log.d("check", "retrieveUserData: "+snapshot.key+" "+username+" "+userId)
                callback(User(userId, username, avatarRef, bio, linkToInstagram, linkToFacebook, linkToTwitter, linkToLinkedIn))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(User("", "", "", "", "", "", "", ""))
            }
        })
    }
    fun updateUserBio(userId: String, bio: String){
        usersRef.child(userId).child("bio").setValue(bio)
    }
    fun updateUserInstagramLink(userId: String, link: String){
        usersRef.child(userId).child("linkToInstagram").setValue(link)
    }
    fun updateUserFacebookLink(userId: String, link: String){
        usersRef.child(userId).child("linkToFacebook").setValue(link)
    }
    fun updateUserTwitterLink(userId: String, link: String){
        usersRef.child(userId).child("linkToTwitter").setValue(link)
    }
    fun updateUserLinkedinLink(userId: String, link: String){
        usersRef.child(userId).child("linkedIn").setValue(link)
    }
    fun createUser(firebaseId: FirebaseUser, user: User, onSuccess: (User) -> Unit, onError: (Exception) -> Unit) {

        //(1..4).joinToString("") { Random.nextInt(0, 10).toString() }
        // Remove non-letter and non-number symbols
        val userData = mapOf(
            "loginId" to firebaseId.uid,
            "profileId" to user.userId,
            "name" to user.username,
            "avatarRef" to user.avatarRef
        )
//        val firebaseIdToProfileId = mapOf(
//            "loginId" to firebaseId.uid,
//            "profileId" to user.userId
//        )
        usersRef.child(user.userId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    usersRef.child(user.userId).setValue(userData).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess(user)
                        } else {
                            onError(Exception("Error creating user"))
                        }
                    }
                } else
                {
                    onError(Exception("User already exists"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(Exception("Error checking user existence"))
            }

        })

    }

    fun createLinkToProfileId(firebaseId: FirebaseUser, user: User, onSuccess: (String) -> Unit) {

        //(1..4).joinToString("") { Random.nextInt(0, 10).toString() }
        // Remove non-letter and non-number symbols
        val firebaseIdToProfileId = mapOf(
            "loginId" to firebaseId.uid,
            "profileId" to user.userId
        )
        usersRef.child(firebaseId.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    usersRef.child(firebaseId.uid).setValue(firebaseIdToProfileId).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess("Link Success")
                        } else {
                            onSuccess("Error creating user")
                        }
                    }
                } else
                {
                    onSuccess("User already exists")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                onSuccess("Error checking user existence")
            }

        })

    }
    fun retrieveLinkToProfileId(firebaseId: FirebaseUser, callback: (String) -> Unit) {
        usersRef.child(firebaseId.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                val profileId = snapshot.child("profileId").getValue(String::class.java).orEmpty()
                Log.d("check", "retrieveLinkToProfile: "+snapshot.key+" "+profileId+" "+firebaseId.uid)
                if(profileId == "")
                    callback("error")
                callback(profileId)
            }
            override fun onCancelled(error: DatabaseError) {
                callback("error")
            }
        })

    }
    fun createChat(user: User, otherUser:User, onSuccess: (String) -> Unit, onError: (Exception) -> Unit){
        val chatId = getChatId(user.userId, otherUser.userId)
        chatsRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Chat does not exist, create a new chat
                    val chatData = mapOf(
                        "user1" to user.userId,
                        "user2" to otherUser.userId
                    )
                    // Create the chat node in the "chats" node
                    chatsRef.child(chatId).setValue(chatData).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Chat created successfully
                            onSuccess(chatId)
                        } else {
                            // Chat creation failed
                            onError(Exception("Error creating chat"))
                        }
                    }
                } else {
                    // Chat already exists
                    onSuccess(chatId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(Exception("Error checking chat existence"))
            }
        })
    }
    fun sendMessage(chatId: String, message: Message) {
        Log.d("check", "sendMessage: "+chatId+" "+message.senderId+" "+message.messageText+" "+message.timestamp)
        val chatRef = chatsRef.child(chatId)
        chatRef.child("unreadCount").addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                var unreadCount = snapshot.getValue(Int::class.java) ?: 0
                unreadCount++
                chatRef.child("unreadCount").setValue(unreadCount)
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
        chatRef.child("messages").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("messages").setValue(true)
                }
                val messagesRef = chatRef.child("messages")
                val messageId = messagesRef.push().key
                val messageMap = mapOf(
                    "senderId" to message.senderId,
                    "messageText" to message.messageText,
                    "timestamp" to message.timestamp
                )
                messagesRef.child(messageId!!).setValue(messageMap)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    fun nullifyUnreadCount(chatId: String){
        val chatRef = chatsRef.child(chatId)
        Log.d("check", "nullifyUnreadCount called")
        chatRef.child("unreadCount").addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                chatRef.child("unreadCount").setValue(0)
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    fun retrieveChats(user: User,callback: (List<Chat>) -> Unit) {
        chatsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = mutableListOf<Chat>()
                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key.orEmpty()

                    if (chatId.contains(user.userId)) {
                        val otherUserId = chatId.replace(user.userId, "").removePrefix("_").removeSuffix("_")
                        val unreadCount = chatSnapshot.child("unreadCount").getValue(Int::class.java) ?: 0
                        val lastMessageSnapshot = chatSnapshot.child("messages").children.lastOrNull()
                        lastMessageSnapshot?.let {
                            val text = it.child("messageText").getValue(String::class.java).orEmpty()
                            val senderId = it.child("senderId").getValue(String::class.java).orEmpty()
                            val timestamp = it.child("timestamp").getValue(Long::class.java) ?: 0
                            //val unreadCount = it.child("unreadCount").getValue(Int::class.java) ?: 0
                            val chat = Chat(chatId, otherUserId, Message(senderId, text, timestamp), unreadCount)
                            chatList.add(chat)
                            callback(chatList)
                        }


                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    fun retrieveMessages(chatId: String, callback: (List<Message>) -> Unit) {
        val messagesRef = chatsRef.child(chatId).child("messages")

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    Log.d("check", "retrieveMessages: "+messageSnapshot.key+" "+message?.senderId+" "+message?.messageText+" "+message?.timestamp)
                    messages.add(message!!)
                }
                callback(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun getChatId(userId1: String, userId2: String): String {
        // Sort the user IDs to create a consistent chat ID
        val sortedUserIds = listOf(userId1, userId2).sorted()
        Log.d("check", "getChatId: "+sortedUserIds[0]+" "+sortedUserIds[1])
        return "${sortedUserIds[0]}_${sortedUserIds[1]}"
    }
}