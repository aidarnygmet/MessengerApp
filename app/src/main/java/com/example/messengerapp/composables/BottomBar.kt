package com.example.messengerapp.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.messengerapp.data.Screen


@Composable
fun NavigationItem(
    navController: NavHostController,
    screen: Screen,
    icon: ImageVector,
    label: String
) {
    var unclickedBackground = MaterialTheme.colorScheme.outline
    var unclickedOutline = MaterialTheme.colorScheme.onSurfaceVariant
    var clickedBackground = MaterialTheme.colorScheme.inverseSurface
    var clickedOutline = MaterialTheme.colorScheme.inverseOnSurface
    var isClicked by remember {
        mutableStateOf(false)
    }
    Column(modifier = Modifier
        .fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        Box(modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)

            .clickable {
                navController.navigate(screen.route)
                isClicked = !isClicked
            }
            , contentAlignment = Alignment.Center){
            Icon(imageVector = icon, contentDescription = label,tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(24.dp)
                    ,
                )
            }
        Text(text = label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
    }
}
