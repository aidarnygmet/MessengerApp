package com.example.messengerapp.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.data.Chat
import com.example.messengerapp.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel: ViewModel() {
    private val _currentUser = mutableStateOf<User?>(null)
    val currentUser: State<User?> = _currentUser
    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> get() = _chats
    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }
    fun getCurrentUser(): User{
        return _currentUser.value!!
    }
    fun setChats(newChats: List<Chat>) {
        _chats.value = newChats
    }
    fun signUp(firebaseManager: FirebaseManager,auth: FirebaseAuth, username: String, email: String, password: String, onComplete: (LoginResult) -> Unit) {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]+\$"
        if(email.matches(emailRegex.toRegex())){
            if(password.matches(passwordRegex.toRegex())){
        auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user!!.sendEmailVerification()
                    .addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            onComplete(LoginResult.Success)
                        } else {
                            // Handle the error
                            val exception = verificationTask.exception
                            onComplete(LoginResult.Failure(exception?.message ?: "Unknown error"))
                        }
                    }



            } else {
                onComplete(LoginResult.Failure(task.exception?.message ?: "Unknown error"))
                Log.w("check", "createUserWithEmail:failure", task.exception)

            }
        }
            } else {
                onComplete(LoginResult.Failure("Invalid password"))
        }
        } else {
            onComplete(LoginResult.Failure("Invalid email"))
        }

    }
    fun signIn(firebaseManager: FirebaseManager, auth: FirebaseAuth, email: String, password: String, username: String = "",onComplete: (LoginResult) -> Unit){
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]+\$"

        if(email.matches(emailRegex.toRegex())) {
            if(password.matches(passwordRegex.toRegex())) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("check", "signInWithEmail:success")
                            val user = auth.currentUser
                            firebaseManager.retrieveLinkToProfileId(user!!) { link ->
                                if(link == "error"){
                                    Log.d("check", "link is error")
                                    if(user.isEmailVerified){
                                        Log.d("check", "email is verified")
                                        var newUser : User
                                        firebaseManager.createUser(
                                            user,
                                            User(username.lowercase().replace(Regex("[^a-zA-Z0-9]"), ""), username, ""),
                                            { savedUser ->
                                                setCurrentUser(savedUser)
                                                newUser = savedUser
                                                firebaseManager.createLinkToProfileId(auth.currentUser!!, newUser) {
                                                    Log.d("check", "createUserWithEmail: $it")
                                                }
                                                onComplete(LoginResult.Success)
                                            },
                                            { e ->
                                                e.message?.let { LoginResult.Failure(it) }?.let { onComplete(it) }
                                            })
                                    } else {
                                        Log.d("check", "email is not verified")
                                        onComplete(LoginResult.Failure("Email not verified"))
                                    }
                                }
                                firebaseManager.retrieveUserData(link) { savedUser ->
                                    setCurrentUser(savedUser)
                                    Log.d("check", "signInWithEmail: ${savedUser.userId}")
                                    onComplete(LoginResult.Success)
                                }
                            }

                        } else {
                            onComplete(
                                LoginResult.Failure(
                                    task.exception?.message ?: "Unknown error"
                                )
                            )
                            Log.w("check", "signInWithEmail:failure", task.exception)
                        }
                    }
            } else {
                onComplete(LoginResult.Failure("Invalid password"))
            }
        } else {
            onComplete(LoginResult.Failure("Invalid email"))
        }
    }
}
sealed class LoginResult {
    object Success : LoginResult()
    data class Failure(val errorMessage: String) : LoginResult()
}