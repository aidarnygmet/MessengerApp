package com.example.messengerapp.composables

import android.annotation.SuppressLint
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.messengerapp.data.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BottomAppBarWithNavigation(navController: NavHostController) {
//    Scaffold(
//            bottomBar = {
//                BottomAppBar(
//                    modifier = Modifier
//                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
//                ) {
//                    Row ( modifier = Modifier.fillMaxSize(),
//                        horizontalArrangement = Arrangement.SpaceEvenly,
//                        verticalAlignment = Alignment.CenterVertically
//                    ){
//                        NavigationItem(navController, Screen.Chats, Icons.Default.Email, "Chats")
//                        NavigationItem(navController, Screen.Calls, Icons.Default.Phone, "Calls")
//                        NavigationItem(navController, Screen.Channels, Icons.Default.Check, "Channels")
//                        NavigationItem(navController, Screen.Profile, Icons.Default.AccountBox, "Profile")
//                    }
//                }
//            },
//
//
//    )

}

@Composable
fun NavigationItem(
    navController: NavHostController,
    screen: Screen,
    icon: ImageVector,
    label: String
) {
    IconButton(
        onClick = { navController.navigate(screen.route) }
    ) {
        Icon(imageVector = icon, contentDescription = label)
    }
}
