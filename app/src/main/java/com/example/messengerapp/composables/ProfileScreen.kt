package com.example.messengerapp.composables

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.messengerapp.FirebaseManager
import com.example.messengerapp.R
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
    var editProfile by remember{ mutableStateOf(false) }
    val currentUser = loginViewModel.getCurrentUser()
    val storageRef = storage.getReference(currentUser.userId)
    val imageUrl by loginViewModel.profilePictureUri.collectAsState()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val uploadTask by remember { mutableStateOf<UploadTask?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }
    val context = LocalContext.current
    var instagramLink by remember {mutableStateOf(loginViewModel.currentUser.value?.linkToInstagram)}
    var showInstagramDialog by remember { mutableStateOf(false) }
    var facebookLink by remember {mutableStateOf(loginViewModel.currentUser.value?.linkToFacebook)}
    var showFacebookDialog by remember { mutableStateOf(false) }
    var twitterLink by remember {mutableStateOf(loginViewModel.currentUser.value?.linkToTwitter)}
    var showTwitterDialog by remember { mutableStateOf(false) }
    var linkedInLink by remember {mutableStateOf(loginViewModel.currentUser.value?.linkToLinkedin)}
    var showLinkedInDialog by remember { mutableStateOf(false) }
    var bio by remember {mutableStateOf(loginViewModel.currentUser.value?.bio)}
    var showBioDialog by remember { mutableStateOf(false) }
    LaunchedEffect(storageRef) {
        storageRef.listAll().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val child = task.result.items
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
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = currentUser.username, style = MaterialTheme.typography.displaySmall)
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
            Column (
                modifier = Modifier
                    .fillMaxSize()
            ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(.3f)
                            .padding(top = 72.dp)
                            .padding(horizontal = 16.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        if(imageUrl ==""){
                            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "", modifier = Modifier.size(150.dp).clip(MaterialTheme.shapes.medium))
                        } else {
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
                        }
                        Column {
                            Box {
                                if (imageUri == null) {
                                    Text(
                                        text = "Update Profile Picture",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(4.dp)
                                            .clickable { launcher.launch("image/*") },
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    if (uploadTask == null) {
                                        Text(
                                            text = "Upload Profile Picture",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .clip(MaterialTheme.shapes.small)
                                                .background(MaterialTheme.colorScheme.secondary)
                                                .padding(4.dp)
                                                .clickable {
                                                    imageUri?.let {
                                                        uploadImage(
                                                            it,
                                                            storageRef,
                                                            currentUser.userId
                                                        )
                                                        { url ->
                                                            loginViewModel.setCurrentUser(
                                                                User(
                                                                    currentUser.userId,
                                                                    currentUser.username,
                                                                    url.toString(),
                                                                    currentUser.bio,
                                                                    currentUser.linkToInstagram,
                                                                    currentUser.linkToFacebook,
                                                                    currentUser.linkToTwitter,
                                                                    currentUser.linkToLinkedin
                                                                )
                                                            )
                                                            if (imageUrl != url.toString()) {
                                                                loginViewModel.setProfilePictureUri(
                                                                    url.toString()
                                                                )
                                                            }
                                                        }
                                                    }
                                                    imageUri = null
                                                },
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                    } else {
                                        CircularProgressIndicator()
                                    }

                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Edit Profile",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(4.dp)
                                    .clickable {
                                        editProfile = !editProfile
                                    },
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                ){
                    Text(text = "Basic Info", style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Column( modifier = Modifier
                        .padding(4.dp)
                    ) {
                        Text(text = "User ID: ${currentUser.userId}", style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .padding(4.dp)
                        )
                        Spacer(modifier =Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
                            Text(text = "Bio: $bio", style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(.7f)
                                    .height(32.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .padding(4.dp)
                            )
                            if(editProfile) {
                                Button(
                                    onClick = { showBioDialog = true },
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(MaterialTheme.shapes.small)
                                ) {
                                    Text(text ="Edit", modifier = Modifier.fillMaxHeight(), style = MaterialTheme.typography.labelLarge)
                                    if (showBioDialog) {
                                        SocialLinkDialog(
                                            social = "bio",
                                            onDismiss = { showBioDialog = false },
                                            onConfirmation = {
                                                showBioDialog = false
                                                updateSocial(0, currentUser.userId, it)
                                                bio = it

                                                Log.d("check", "bio: $it")
                                            }
                                        )
                                    }
                                }
                            }
                        }

                    }

                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, bottom = 96.dp)
                        .padding(horizontal = 16.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                ) {
                    Text(text = "Social", style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.tertiary
                    )
                    Column ( modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(4.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier
                                .size(24.dp))
                            Text(text = "Instagram: $instagramLink", style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth(.7f)
                                    .fillMaxHeight()
                                    .padding(6.dp)
                                    .clickable {
                                        val actualLink = "https://www.instagram.com/$instagramLink"
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, Uri.parse(actualLink))
                                        context.startActivity(intent)
                                    },
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if(editProfile) {
                                Button(
                                    onClick = { showInstagramDialog = true },
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                ) {
                                    Text(text ="Edit", modifier = Modifier.fillMaxHeight(), style = MaterialTheme.typography.labelLarge)
                                    if (showInstagramDialog) {
                                        SocialLinkDialog(
                                            social = "Instagram",
                                            onDismiss = { showInstagramDialog = false },
                                            onConfirmation = {
                                                showInstagramDialog = false
                                                updateSocial(1, currentUser.userId, it)
                                                instagramLink = it

                                                Log.d("check", "instagram link: $it")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier
                                .size(24.dp))
                            Text(text = "Facebook: $facebookLink", style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth(.7f)
                                    .fillMaxHeight()
                                    .padding(6.dp)
                                    .clickable {
                                        val actualLink = "https://www.facebook.com/$facebookLink"
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, Uri.parse(actualLink))
                                        context.startActivity(intent)
                                    },
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if(editProfile){
                                Button(onClick = { showFacebookDialog = true },
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(MaterialTheme.shapes.medium)) {
                                    Text(text ="Edit", modifier = Modifier.fillMaxHeight(), style = MaterialTheme.typography.labelLarge)
                                    if (showFacebookDialog){
                                        SocialLinkDialog(
                                            social = "Facebook",
                                            onDismiss = { showFacebookDialog = false },
                                            onConfirmation = {showFacebookDialog = false
                                                updateSocial(2, currentUser.userId, it)
                                                facebookLink = it
                                                Log.d("check", "facebook link: $it")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(24.dp))
                            Text(text = "Twitter: $twitterLink", style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth(.7f)
                                    .fillMaxHeight()
                                    .padding(6.dp)
                                    .clickable {
                                        val actualLink = "https://www.twitter.com/$twitterLink"
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, Uri.parse(actualLink))
                                        context.startActivity(intent)
                                    },
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if(editProfile){
                                Button(onClick = { showTwitterDialog = true },
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(MaterialTheme.shapes.medium)) {
                                    Text(text ="Edit", modifier = Modifier.fillMaxHeight(), style = MaterialTheme.typography.labelLarge)
                                    if (showTwitterDialog){
                                        SocialLinkDialog(
                                            social = "Facebook",
                                            onDismiss = { showTwitterDialog = false },
                                            onConfirmation = {showTwitterDialog = false
                                                updateSocial(3, currentUser.userId, it)
                                                twitterLink = it

                                                Log.d("check", "twitter link: $it")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(24.dp))
                            Text(text = "LinkedIn: $linkedInLink", style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth(.7f)
                                    .fillMaxHeight()
                                    .padding(6.dp)
                                    .clickable {
                                        val actualLink = "https://www.linkedin.com/in/$linkedInLink"
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, Uri.parse(actualLink))
                                        context.startActivity(intent)
                                    },
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if(editProfile) {
                                Button(
                                    onClick = { showLinkedInDialog = true },
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                ) {
                                    Text(text ="Edit", modifier = Modifier.fillMaxHeight(), style = MaterialTheme.typography.labelLarge)
                                    if (showLinkedInDialog) {
                                        SocialLinkDialog(
                                            social = "Facebook",
                                            onDismiss = { showLinkedInDialog = false },
                                            onConfirmation = {
                                                showLinkedInDialog = false
                                                updateSocial(4, currentUser.userId, it)
                                                linkedInLink = it
                                                Log.d("check", "linkedIn link: $it")
                                            }
                                        )
                                    }
                                }
                            }
                        }

                    }
                }


            }
        })


}
private fun updateSocial(social: Int, userId: String, link: String){
    val firebaseManager = FirebaseManager()
    when (social) {
        0-> {
            firebaseManager.updateUserBio(userId, link)
        }
        1 -> {
            firebaseManager.updateUserInstagramLink(userId, link)
        }
        2 -> {
            firebaseManager.updateUserFacebookLink(userId, link)
        }
        3 -> {
            firebaseManager.updateUserTwitterLink(userId, link)
        }
        4 -> {
            firebaseManager.updateUserLinkedinLink(userId, link)

        }
    }

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
        //val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
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