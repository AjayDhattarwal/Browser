package com.ar.idm.ui.screen.menu.settings.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ar.idm.R
import com.ar.idm.ui.components.CommonCardRowFrame
import com.ar.idm.ui.navigation.AppDestination
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CurrentUrlCard(
    textState: StateFlow<TextFieldValue>,
    modifier: Modifier = Modifier,
    data: AppDestination.Search,
    updateText: (String) -> Unit = {},
    navigateBack: () -> Unit = {}
) {

    val textFieldValue by  textState.collectAsState()

    val title by remember { mutableStateOf(data.title) }
    val url by remember {mutableStateOf(data.url)}
    val favIconUrl by remember {mutableStateOf(data.favIconUrl)}

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if(title != null && url != null && textFieldValue.text.isEmpty()){

        CommonCardRowFrame(
            modifier = modifier
                .height(55.dp)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    navigateBack()
                },
            contentHorizontalPadding = 0.dp,
            leadingIcon = {
                IconButton(
                    onClick = {}
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(favIconUrl),
                        contentDescription = "Favicon",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title ?: "Untitled",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = url ?: "Untitled",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, url)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Sharing link")
                        context.startActivity(shareIntent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share url"
                    )
                }
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(url!!))
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = "copy"
                    )
                }
                IconButton(
                    onClick = { updateText(url!!) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
        )
    }

    if(textFieldValue.text.isEmpty()){
        TextClipBoardCard(
            onSelect = updateText
        )
    }

}