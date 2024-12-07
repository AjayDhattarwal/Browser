package com.ar.webwiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun CommonImageFrame(
    modifier: Modifier = Modifier,
    model: Any,
    contentDescription: String = "image",
    spacerHeight: Dp = 2.dp,
    imageSize: Dp = 24.dp,
    imagePadding: Dp = 10.dp,
    title: String? =  null,
    titleStyle: TextStyle = MaterialTheme.typography.labelMedium,
    onClick: () -> Unit
){
    Column(
        modifier = modifier
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.clip(CircleShape)
                .size(imageSize)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(0.5f)),
            contentAlignment = Alignment.Center
        ){
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize().padding(imagePadding)
            )
        }
        Spacer(Modifier.height(spacerHeight))
        if (title != null) {
            Text(
                text = title,
                style = titleStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}