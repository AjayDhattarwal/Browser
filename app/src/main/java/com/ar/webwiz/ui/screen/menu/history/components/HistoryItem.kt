package com.ar.webwiz.ui.screen.menu.history.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ar.webwiz.ui.components.CommonItemFrame

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