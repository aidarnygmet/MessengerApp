package com.example.messengerapp.composables

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.data.BottomNavigationItem
import com.example.messengerapp.data.Chat
import com.example.messengerapp.data.Screen
import com.example.messengerapp.data.User
import com.example.messengerapp.ui.theme.MessengerAppTheme
import com.example.messengerapp.viewModel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation(auth: FirebaseAuth, loginViewModel: LoginViewModel){
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
        val firebaseManager = FirebaseManager()
        val currentUser = auth.currentUser
        val items = listOf(
            BottomNavigationItem(
                title = "Chats",
                selectedIcon = Icons.Filled.Email,
                unselectedIcon = Icons.Outlined.Email,
                hasNews = false
            ),
            BottomNavigationItem(
                title = "Calls",
                selectedIcon = Icons.Filled.Call,
                unselectedIcon = Icons.Outlined.Call,
                hasNews = false
            ),
            BottomNavigationItem(
                title = "Groups",
                selectedIcon = Icons.Filled.Star,
                unselectedIcon = Icons.Outlined.Star,
                hasNews = false
            ),
            BottomNavigationItem(
                title = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                hasNews = false
            ),
        )
        var selected by rememberSaveable { mutableIntStateOf(0) }
        var showDialog by remember{ mutableStateOf(false) }
        Scaffold(
            bottomBar = {
                if (bottomState.component1().first){
                    NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(selected = selected==index,
                            onClick = {
                                      selected = index
                                when(index){
                                    0 -> navController.navigate(Screen.Chats.route)
                                    1 -> navController.navigate(Screen.Calls.route)
                                    2 -> navController.navigate(Screen.Channels.route)
                                    3 -> navController.navigate(Screen.Profile.route)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if(selected==index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(text = item.title)
                            })
                    }
                }
                }
//                if(bottomState.component1().first){
//                    BottomAppBar(
//                        modifier = Modifier
//                            .background(MaterialTheme.colorScheme.surface)
//                    ) {
//                        Row ( modifier = Modifier.fillMaxSize(),
//                            horizontalArrangement = Arrangement.SpaceEvenly,
//                            verticalAlignment = Alignment.CenterVertically
//                        ){
//                            NavigationItem(navController, Screen.Chats, Icons.Outlined.MailOutline, "Chats")
//                            NavigationItem(navController, Screen.Calls, Icons.Outlined.Phone, "Calls")
//                            NavigationItem(navController, Screen.Channels, Icons.Outlined.Check, "Channels")
//                            NavigationItem(navController, Screen.Profile, Icons.Outlined.Person, "Profile")
//                        }
//                    }
//                }

            },
            floatingActionButton = {
                if(bottomState.component1().second == 1){
                FloatingActionButton(
                    onClick = { 
                        //navController.navigate(Screen.CreateChatScreen.route)
                        showDialog = true
                              },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    if(showDialog){
                        NewChatDialog(onDismissRequest = { showDialog = false }, onConfirmation = {
                            showDialog=false
                            createChatAndNavigate(navController, it, loginViewModel.getCurrentUser())
                        })
                    }

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
                            ChatsScreen(
                                navController = navController,
                                firebaseManager = firebaseManager,
                                loginViewModel = loginViewModel
                            )
                        }
                        composable(
                            Screen.Calls.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            CallsScreen(navController = navController)
                        }
                        composable(
                            Screen.LoadingScreen.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            LoadingScreen()
                        }
                        composable(
                            Screen.Channels.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            ChannelsScreen(navController = navController)
                        }
                        composable(
                            Screen.Profile.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
                            ProfileScreen(navController = navController, loginViewModel)
                        }
                        composable(
                            Screen.LogIn.route,
                            enterTransition = { fadeIn(animationSpec = tween(700)) },
                            exitTransition = { fadeOut(animationSpec = tween(700)) }
                        ) {
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
                        composable(Screen.ChatWithDetails.route + "/{chatId}",
                            arguments = listOf(
                                navArgument("chatId") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            var otherUserId = ""
                            val chatId = backStackEntry.arguments?.getString("chatId")
                            val parts = chatId!!.split("_")

                            if (parts[0]==loginViewModel.currentUser.value!!.userId) {
                                otherUserId = parts[1]
                            } else if (parts[1]==loginViewModel.currentUser.value!!.userId) {
                                otherUserId = parts[0]
                            }
                            val chat = Chat(
                                otherUserId = otherUserId,
                                chatId = chatId,
                                lastMessage = null,
                                unreadCount = null
                            )
                            Log.d("check", "Launching Chat Detail Screen: ${loginViewModel.currentUser.value}")
                            ChatDetailScreen(
                                navController = navController,
                                chat = chat,
                                loginViewModel = loginViewModel
                            )
                        }
                        composable(
                            "create_chat_screen"
                        ) {
                            Log.d("check", "Launching Create Chats Screen: ${loginViewModel.currentUser.value}")
                            CreateChatScreen(
                                navController = navController,
                                loginViewModel = loginViewModel
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
                        composable(Screen.CameraScreen.route){
                            Log.d("check", "Launching Camera Screen")
                            CameraScreen()
                        }
                    }

            }
        )
}
}
private fun createChatAndNavigate(navController: NavController, otherUserId: String, user: User) {
    Log.d("check", "createChatAndNavigate: $otherUserId")
    val firebaseManager = FirebaseManager()
    firebaseManager.retrieveUserData(otherUserId){
            otherUser ->
        if(otherUser.userId == ""){
            Log.d("check", "createChatAndNavigate: User not found")
            return@retrieveUserData
        }
        firebaseManager.createChat(user, otherUser,{ chatId ->
            navController.navigate(Screen.ChatWithDetails.withArgs(chatId))
        }, {
                e -> Log.d("check", "createChatAndNavigate: $e")
        })
    }
}