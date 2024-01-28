package com.example.messengerapp.composables

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.viewModel.LoginViewModel
import com.example.messengerapp.data.Chat
import com.example.messengerapp.data.Screen
import com.example.messengerapp.ui.theme.MessengerAppTheme
import com.example.messengerapp.viewModel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation(auth: FirebaseAuth, loginViewModel: LoginViewModel, chatViewModel: ChatViewModel){
    MessengerAppTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val bottomState = rememberSaveable { (mutableStateOf(Pair<Boolean, Int>(false, 0))) }
        when (navBackStackEntry?.destination?.route) {
            "chats" -> {
                bottomState.value=Pair(true, 1)
            }
            "calls" -> {
                bottomState.value=Pair(true, 2)
            }
            "channels" -> {
                bottomState.value=Pair(true, 3)
            }
            "profile" -> {
                bottomState.value=Pair(true, 0)
            }
            else -> {
                bottomState.value=Pair(false, 0)
            }
        }
        var isUserSet by remember{ mutableStateOf(false) }
        val firebaseManager = FirebaseManager()
        val currentUser = auth.currentUser
        LaunchedEffect(Unit){
            if (currentUser != null) {
                firebaseManager.retrieveLinkToProfileId(currentUser){
                    firebaseManager.retrieveUserData(it) {it1 ->
                        loginViewModel.setCurrentUser(it1)
                        isUserSet=true
                        Log.d("check", "is it here ${loginViewModel.currentUser.value}")
                    }
                }
            }
        }

        if(!isUserSet && currentUser != null){
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        } else{
        Scaffold(
            bottomBar = {
                if(bottomState.component1().first){
                    BottomAppBar(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                    ) {
                        Row ( modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            NavigationItem(navController, Screen.Chats, Icons.Default.Email, "Chats")
                            NavigationItem(navController, Screen.Calls, Icons.Default.Phone, "Calls")
                            NavigationItem(navController, Screen.Channels, Icons.Default.Check, "Channels")
                            NavigationItem(navController, Screen.Profile, Icons.Default.AccountBox, "Profile")
                        }
                    }
                }

            },
            floatingActionButton = {
                if(bottomState.component1().second == 1){
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateChatScreen.route)},
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            content = {

                    NavHost(
                        navController = navController,
                        startDestination = if(currentUser == null) Screen.LogIn.route else if(loginViewModel.currentUser.value == null) Screen.LoadingScreen.route else Screen.Chats.route,
                   ) {

                        composable(
                            Screen.Chats.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            Log.d("check", "Launching Chats Screen: ${loginViewModel.currentUser.value}")
                            ChatsScreen(
                                navController = navController,
                                firebaseManager = firebaseManager,
                                loginViewModel = loginViewModel,
                                chatViewModel = chatViewModel
                            )
                        }
                        composable(
                            Screen.Calls.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            Log.d("check", "Launching Calls Screen: ${loginViewModel.currentUser.value}")
                            CallsScreen(navController = navController)
                        }
                        composable(
                            Screen.LoadingScreen.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            Log.d("check", "Launching Loading Screen: ${loginViewModel.currentUser.value}")
                            LoadingScreen()
                        }
                        composable(
                            Screen.Channels.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            Log.d("check", "Launching Channels Screen: ${loginViewModel.currentUser.value}")
                            ChannelsScreen(navController = navController)
                        }
                        composable(
                            Screen.Profile.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            Log.d("check", "Launching Profile Screen: ${loginViewModel.currentUser.value}")
                            ProfileScreen(navController = navController, loginViewModel)
                        }
                        composable(
                            Screen.LogIn.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            Log.d("check", "Launching Log in Screen: ${loginViewModel.currentUser.value}")
                            LoginScreen(firebaseManager, auth, navController, loginViewModel)
                        }
                        composable(
                            Screen.SignUp.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            Log.d("check", "Launching Sign up Screen: ${loginViewModel.currentUser.value}")
                            SignUpScreen(auth, navController, loginViewModel, firebaseManager)
                        }
                        composable(Screen.ChatWithDetails.route + "/{chatId}/{otherUserId}/{otherUserName}",
                            arguments = listOf(
                                navArgument("chatId") {
                                    type = NavType.StringType
                                },
                                navArgument("otherUserId") {
                                    type = NavType.StringType
                                },
                                navArgument("otherUserName") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val otherUserId = backStackEntry.arguments?.getString("otherUserId")
                            val chatId = backStackEntry.arguments?.getString("chatId")
                            val chat = Chat(
                                otherUserId = otherUserId!!,
                                chatId = chatId!!,
                                lastMessage = null,
                                timestamp = null
                            )
                            Log.d("check", "Launching Chat Detail Screen: ${loginViewModel.currentUser.value}")
                            ChatDetailScreen(
                                navController,
                                chat = chat,
                                firebaseManager = firebaseManager,
                                loginViewModel,
                                chatViewModel
                            )
                        }
                        composable(
                            "create_chat_screen"
                        ) {
                            Log.d("check", "Launching Create Chats Screen: ${loginViewModel.currentUser.value}")
                            CreateChatScreen(
                                navController = navController,
                                firebaseManager = firebaseManager,
                                loginViewModel = loginViewModel,
                                chatViewModel = chatViewModel
                            )
                        }
                        composable(Screen.VerificationScreen.route + "/{email}/{password}/{username}",
                            arguments = listOf(
                                navArgument("email") {
                                    type = NavType.StringType
                                },
                                navArgument("password") {
                                    type = NavType.StringType
                                },
                                navArgument("username") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email")?:""
                            val password = backStackEntry.arguments?.getString("password")?:""
                            val username = backStackEntry.arguments?.getString("username")?:""
                            Log.d("check", "Launching Verification Screen: ${loginViewModel.currentUser.value}")
                                VerificationScreen(
                                    navController = navController,
                                    firebaseManager = firebaseManager,
                                    loginViewModel = loginViewModel,
                                    auth = auth,
                                    email = email,
                                    password = password,
                                    username = username
                                )

                        }

                    }

            }
        )}
}
}