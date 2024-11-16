package com.ar.idm.ui.screen.menu.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ar.idm.R
import com.ar.idm.ui.components.CommonCardRowFrame

@Composable
fun TextClipBoardCard(
    onSelect: (String) -> Unit
){
    val clipboardManager = LocalClipboardManager.current

    var annotatedString by remember { mutableStateOf(clipboardManager.getText()) }
    var textVisibility by remember { mutableStateOf(false) }

    annotatedString?.let {

        CommonCardRowFrame(
            modifier = Modifier
                .height(65.dp)
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    annotatedString = clipboardManager.getText()
                    annotatedString?.let { onSelect(it.text) }
                },
            contentHorizontalPadding = 0.dp,
            leadingIcon = {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            },
            content = {
                Column {
                    Text(
                        text = "Text you copied",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if(textVisibility){
                        Text(
                            text = it.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        annotatedString = clipboardManager.getText()
                        textVisibility = !textVisibility
                    }
                ) {
                    Icon(
                        painter = painterResource(if(textVisibility) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                        contentDescription = "view"
                    )
                }
            }
        )

    }


}