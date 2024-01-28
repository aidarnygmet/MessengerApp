package com.example.messengerapp.data

sealed class Screen(val route: String) {
    object Chats : Screen("chats")
    object Calls : Screen("calls")
    object Channels : Screen("channels")
    object Profile : Screen("profile")
    object LogIn : Screen("login")
    object SignUp : Screen("signup")
    object ChatWithDetails : Screen("chat_detail_screen")
    object CreateChatScreen : Screen("create_chat_screen")
    object LoadingScreen : Screen("loading_screen")
    object VerificationScreen: Screen("verification_screen")
    object CameraScreen: Screen("camera_screen")
    fun withArgs(vararg args: String): String{
        return buildString{
            append(route)
            args.forEach {
                append("/$it")
            }
        }
    }


}
