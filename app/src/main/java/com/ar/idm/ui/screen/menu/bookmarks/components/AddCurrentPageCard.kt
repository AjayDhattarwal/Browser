package com.ar.idm.ui.screen.menu.bookmarks.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ar.idm.R
import com.ar.idm.ui.components.CommonCardRowFrame

@Composable
fun AddCurrentPageCard(
    modifier: Modifier,
    favIconUrl: String?,
    title: String?,
    url: String?,
    onClick: () -> Unit = {},
    addToBookmark: () -> Unit = {},
){
    val context = LocalContext.current

    CommonCardRowFrame(
        modifier = modifier
            .height(55.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentHorizontalPadding = 0.dp,
        leadingIcon = {
            IconButton(
                onClick = {}
            ) {
                Box(
                    Modifier.size(24.dp)
                ){
                    Image(
                        painter = rememberAsyncImagePainter(favIconUrl),
                        contentDescription = "Favicon",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
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
                onClick = addToBookmark
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark),
                    contentDescription = "Add to Bookmark",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

        }
    )
}