package com.ar.webwiz.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun CommonTextFrame(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    subTitle: String? = null,
    subTitleStyle: TextStyle = MaterialTheme.typography.labelSmall,
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ){
        Text(
            text = title,
            style = titleStyle,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if(subTitle != null) {
            Text(
                text = subTitle,
                style = subTitleStyle,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    }
}