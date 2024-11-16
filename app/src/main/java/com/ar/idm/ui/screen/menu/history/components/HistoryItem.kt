package com.ar.idm.ui.screen.menu.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ar.idm.ui.components.CommonItemFrame

@Composable
fun HistoryItem(
    modifier: Modifier = Modifier,
    image: Any,
    title: String,
    subtitle: String,
    onClick: (String) -> Unit,
    onClear: () -> Unit
){
    CommonItemFrame(
        modifier = modifier,
        image = image,
        title = title,
        subtitle = subtitle,
        onClick = onClick
    ) {
        IconButton(
            onClick = onClear,
            modifier = Modifier.size(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "clear"
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HistoryItemPreview(){
    HistoryItem(
        image = "https://www.google.com/favicon.png",
        title = "Google",
        subtitle = "https://www.google.com",
        onClick = {},
        onClear = {}
    )

}