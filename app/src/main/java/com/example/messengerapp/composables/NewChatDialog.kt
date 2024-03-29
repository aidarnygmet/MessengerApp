package com.example.messengerapp.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (String) -> Unit,
){
    var otherUserId by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = { onDismissRequest }) {
        Column {
            Icon(imageVector = Icons.Default.Person, contentDescription = null)
            Text(text = "Enter User ID to start a new chat\nNote: User ID is lowercase Username without spaces and special symbols")
            OutlinedTextField(
                value = otherUserId,
                onValueChange = { otherUserId = it },
                label = { Text(text = "Enter Other User ID") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onConfirmation(otherUserId)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Row {
                Button(onClick = { onDismissRequest() }) {
                    Text(text = "Cancel")
                }
                Button(onClick = { onConfirmation(otherUserId) }) {
                    Text(text = "Confirm")
                }
            }
        }

    }
}