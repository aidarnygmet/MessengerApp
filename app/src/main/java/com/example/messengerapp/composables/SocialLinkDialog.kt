package com.example.messengerapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialLinkDialog(social: String, onDismiss: () -> Unit, onConfirmation: (String) -> Unit) {
    var link by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = { onDismiss() }, modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 8.dp))) {
        Column {
            if(social == "bio"){
                Text(text = "Enter your $social to update your bio", color = MaterialTheme.colorScheme.onSurface)
            } else {
                Text(text = "Enter your $social ID to link with your account", color = MaterialTheme.colorScheme.onSurface)
            }
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = {
                    if(social == "bio"){
                        Text(text = "Your Bio", color = MaterialTheme.colorScheme.onSurface)
                    } else {
                        Text(text = "Your Link", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onConfirmation(link)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Row {
                Button(onClick = { onDismiss() }) {
                    Text(text = "Cancel")
                }
                Button(onClick = { onConfirmation(link) }) {
                    Text(text = "Confirm")
                }
            }
        }
    }
}