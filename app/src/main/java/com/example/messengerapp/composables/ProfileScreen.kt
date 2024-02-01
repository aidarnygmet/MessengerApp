package com.example.messengerapp.composables

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.messengerapp.data.Screen
import com.example.messengerapp.data.User
import com.example.messengerapp.viewModel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, loginViewModel: LoginViewModel) {
    val storage = FirebaseStorage.getInstance()

    val currentUser = loginViewModel.getCurrentUser()
    val storageRef = storage.getReference(currentUser.userId)
    val imageUrl by loginViewModel.profilePictureUri.collectAsState()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadTask by remember { mutableStateOf<UploadTask?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }
    LaunchedEffect(storageRef) {
        storageRef.listAll().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val child = task.result.items // Access the first element or null if empty
                Log.d("check", "userID ${currentUser.userId}")
                if (child.size != 0) {
                    child[0].downloadUrl.addOnSuccessListener { uri ->
                        if (imageUrl != uri.toString()) {
                            loginViewModel.setProfilePictureUri(uri.toString())
                        }
                    }.addOnFailureListener {
                        Log.d("check", "ProfileScreen: failed to get image url: $storageRef and error: $it")
                    }
                } else {
                    Log.d("check", "ProfileScreen2: failed to get image url: $storageRef ")
                }
            } else {
                Log.d("check", "ProfileScreen3: failed to get image url: $storageRef")
            }
        }
    }
    Scaffold (
        topBar = {
            Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically){

                Text(text = "Profile", style = MaterialTheme.typography.displayMedium)
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),horizontalArrangement = Arrangement.End){
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = null,
                        modifier = Modifier.clickable {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.LogIn.route)
                        })
                }
            }
        }, content = {
            Column {
                if(imageUrl == null){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(.3f)
                            .padding(top = 32.dp)
                            .padding(24.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "placeholder",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(shape = MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    launcher.launch("image/*")
                                }
                                .padding(8.dp)
                        )
                        Button(
                            onClick = { imageUri?.let { uploadImage(it, storageRef, currentUser.userId){ url ->
                                Log.d("check", "calling onCallback")
                                loginViewModel.setCurrentUser(User(currentUser.userId, currentUser.username, url.toString(), currentUser.bio, currentUser.linkToInstagram, currentUser.linkToFacebook, currentUser.linkToTwitter, currentUser.linkToLinkedin))
                                if (imageUrl != url.toString()) {
                                    loginViewModel.setProfilePictureUri(url.toString())
                                }
                            } } },
                            enabled = imageUri != null && uploadTask == null
                        ) {
                            if (uploadTask == null) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                else{
                    Log.d("check", "ProfileScreen AvatarRef: ${currentUser.avatarRef} \nimage url: $imageUrl")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(.3f)
                            .padding(top = 32.dp)
                            .padding(24.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl.toUri())
                                .build(),
                            contentDescription = "",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop,
                        )
//                        Column(horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.SpaceEvenly
//                        ) {
//                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
//                            Text(text ="Change Picture", style = MaterialTheme.typography.bodySmall)
//                        }
                        Box(){
                            if(imageUri == null){
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "placeholder",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(shape = MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            // Launch the image picker

                                            launcher.launch("image/*")
                                        }
                                        .padding(8.dp)
                                )
                            } else {
                                if (uploadTask == null) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null,
                                        modifier = Modifier
                                            .size(150.dp)
                                            .clip(shape = MaterialTheme.shapes.medium)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable {
                                                imageUri?.let { uploadImage(it, storageRef, currentUser.userId)
                                                { url ->
                                                    Log.d("check", "calling onCallback")
                                                    loginViewModel.setCurrentUser(User(currentUser.userId, currentUser.username, url.toString(), currentUser.bio, currentUser.linkToInstagram, currentUser.linkToFacebook, currentUser.linkToTwitter, currentUser.linkToLinkedin))
                                                    if (imageUrl != url.toString()) {
                                                        loginViewModel.setProfilePictureUri(url.toString())
                                                    }
                                                }
                                                }
                                                imageUri=null
                                            }
                                            .padding(8.dp))
                                } else {
                                    CircularProgressIndicator()
                                }

                            }
                        }
//                        Button(
//                            onClick = {
//                                launcher.launch("image/*")
//                                      },
//                            //enabled = imageUri != null && uploadTask == null
//                        ) {
//                            if (uploadTask == null) {
//                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
//                            } else {
//                                CircularProgressIndicator()
//                            }
//                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                ){
                    Text(text = "Basic Info", style = MaterialTheme.typography.displaySmall)
                    Column( modifier = Modifier
                        .padding(4.dp)
                    ) {
                        Text(text = "Username: ${currentUser.username}", style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp)
                        )
                        Spacer(modifier =Modifier.height(8.dp))
                        Text(text = "User ID: ${currentUser.userId}", style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp)
                        )
                        Spacer(modifier =Modifier.height(8.dp))
                        Text(text = "Bio: placeholder bio", style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp)
                        )
                    }

                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                ) {
                    Text(text = "Social", style = MaterialTheme.typography.displaySmall)
                    Column ( modifier = Modifier
                        .padding(4.dp)
                    ) {
                        Text(text = "Instagram: ", style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp)
                        )
                        Spacer(modifier =Modifier.height(8.dp))
                        Text(text = "Facebook: ", style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp)
                        )
                        Spacer(modifier =Modifier.height(8.dp))
                        Text(text = "Twitter: ", style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp)
                        )
                    }
                }


            }
        })


}

private fun uploadImage(imageUri: Uri, storageRef: StorageReference, userId: String, onCallback: (Uri) -> Unit) {
    // Generate a unique filename for the image
    val filename = UUID.randomUUID().toString()
    val imagesRef = storageRef.child(filename)
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
    // Upload the image to Firebase Storage
    val uploadTask = imagesRef.putFile(imageUri)

    // Monitor the upload progress
    uploadTask.addOnProgressListener { taskSnapshot ->
        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
        // Handle progress updates if needed
    }

    // Handle successful upload
    uploadTask.addOnSuccessListener {

        storageRef.listAll().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val child = task.result.items // Access the first element or null if empty
                if (child.size != 0) {
                    child[0].downloadUrl.addOnSuccessListener { uri ->
                        onCallback(uri)
                        databaseReference.child("avatarRef").setValue(uri.toString())
                    }.addOnFailureListener {
                        Log.d("check", "uploadTask: failed to get image url: $storageRef and error: $it")
                    }
                } else {
                    Log.d("check", "uploadTask2: failed to get image url: $storageRef ")
                }
            } else {
                Log.d("check", "uploadTask3: failed to get image url: $storageRef")
            }
        }

        Log.d("check", "upload Successful")
        // Handle successful upload if needed
    }

    // Handle upload failure
    uploadTask.addOnFailureListener { exception ->
        Log.d("check", "upload failed")
    }
}