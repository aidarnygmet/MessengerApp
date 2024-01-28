package com.example.messengerapp.composables

import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun CameraScreen() {
    var controller = LifecycleCameraController(LocalContext.current).apply {
        setEnabledUseCases(CameraController.IMAGE_CAPTURE)
    }
    CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())
}