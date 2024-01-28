package com.example.messengerapp.composables

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.UUID


@Composable
fun ProfilePictureScreen(storageRef: StorageReference, userId: String, onCallback: (Uri)->Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadTask by remember { mutableStateOf<UploadTask?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .build(),
                contentDescription = "",
                modifier = Modifier
                    .padding(4.dp)
                    .size(150.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "placeholder",
                modifier = Modifier
                    .size(200.dp)
                    .clip(shape = MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.background)
                    .clickable {
                        // Launch the image picker
                        launcher.launch("image/*")
                    }
                    .padding(8.dp)
            )

        }

        // Upload button
        Button(
            onClick = { imageUri?.let { uploadImage(it, storageRef, userId){ url ->
                Log.d("check", "calling onCallback")
                onCallback(url)} } },
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