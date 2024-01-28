package com.example.messengerapp.composables

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.viewModel.LoginResult
import com.example.messengerapp.viewModel.LoginViewModel
import com.example.messengerapp.data.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(auth: FirebaseAuth, navController: NavHostController, loginViewModel: LoginViewModel, firebaseManager: FirebaseManager){
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { navController.navigate(Screen.LogIn.route) }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),
            leadingIcon = {
                Icon(Icons.Default.MailOutline, contentDescription = null)
            }
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),
            leadingIcon = {
                Icon(Icons.Default.MailOutline, contentDescription = null)
            }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            }
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick =
            {
                if(password!=confirmPassword){
                    Toast.makeText(navController.context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                loginViewModel.signUp(firebaseManager, auth, username, email, password) {
                    when (it) {
                        is LoginResult.Success -> {
                            Log.d("check", "Verification email sent")
                            navController.navigate(Screen.VerificationScreen.withArgs(email, password, username))
                        }
                        is LoginResult.Failure -> {
                            if(it.errorMessage == "Error creating user"){
                                Toast.makeText(navController.context, "Error creating user", Toast.LENGTH_SHORT).show()
                            } else if(it.errorMessage == "User already exists"){
                                Toast.makeText(navController.context, "User already exists", Toast.LENGTH_SHORT).show()
                            } else if(it.errorMessage == "Error checking user existence"){
                                Toast.makeText(navController.context, "Error checking user existence", Toast.LENGTH_SHORT).show()
                            }else if(it.errorMessage == "Invalid password"){
                                Toast.makeText(navController.context, "Invalid password", Toast.LENGTH_SHORT).show()
                            } else if(it.errorMessage == "Invalid email"){
                                Toast.makeText(navController.context, "Invalid email", Toast.LENGTH_SHORT).show()
                            } else if(it.errorMessage == "The email address is already in use by another account."){
                                Toast.makeText(navController.context, "The email address is already in use by another account.", Toast.LENGTH_SHORT).show()
                            }
                            Log.d("check", "Error: ${it.errorMessage}")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign Up")
        }
    }
}
