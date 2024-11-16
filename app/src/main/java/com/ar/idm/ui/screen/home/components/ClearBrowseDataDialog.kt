package com.ar.idm.ui.screen.home.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ar.idm.R
import com.ar.idm.utils.function.applyBlur

@Composable
fun ClearBrowseDataDialog(
    onDismissRequest: () -> Unit,
    onClearData: () -> Unit,
    lastVisitedUrl: String,
    tabCount: Int,
    image: ImageBitmap? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Last 15 min") }

    val timeOptions = listOf(
        "Last 15 min", "Last hour", "Last 24 hours",
        "Last 7 days", "Last 4 weeks", "All time"
    )

    var size: IntSize? by remember { mutableStateOf(null) }
    var position: Offset? by remember { mutableStateOf(null) }

    var isPositionInitialized by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val newImage = if (image != null && position != null && size != null) {
        cropBitmapCenter(image.asAndroidBitmap(), size!!.width, size!!.height) } else null

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .border(
                    BorderStroke(1.dp, color = Color.Gray.copy(0.6f)),
                    MaterialTheme.shapes.medium
                )
        ) {



            newImage?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .applyBlur(180f),
                    colorFilter = ColorFilter.tint(
                        color = Color.DarkGray.copy(0.7f),
                        blendMode = BlendMode.Multiply
                    )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        if (!isPositionInitialized) {
                            size = layoutCoordinates.size
                            position = layoutCoordinates.localToScreen(Offset.Zero)
                            isPositionInitialized = true
                        }
                    },
            ){
                Text(
                    text = "Clear browsing data",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    Row(
                        modifier = Modifier
                            .clickable { expanded = !expanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedOption,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "time range",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timeOptions.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedOption = option
                                    expanded = false
                                },
                                text = { Text(option) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = "History",
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.85f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = lastVisitedUrl,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tabs),
                        contentDescription = "Tabs",
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.85f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$tabCount Tabs on this device",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cookie),
                        contentDescription = "Cookies and cache",
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.85f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cookies, cache, and other site data",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.85f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp)
                    ) {
                        Text("More Options")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "more options"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text("Cancel", color = Color.LightGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onClearData,
                    ) {
                        Text("Clear Data")
                    }
                }
            }
        }
    }

}

@Preview
@Composable
fun ClearBrowseDataDialogPreview() {
    ClearBrowseDataDialog(
        onDismissRequest = {},
        onClearData = {},
        lastVisitedUrl = "https://example.com",
        tabCount = 3,
    )
}



fun cropBitmapCenter(originalBitmap: Bitmap, cropWidth: Int, cropHeight: Int): ImageBitmap {

    var startX = (originalBitmap.width - cropWidth) / 2
    var startY = (originalBitmap.height - cropHeight) / 2

    var validCropWidth = cropWidth.coerceAtMost(originalBitmap.width)
    var validCropHeight = cropHeight.coerceAtMost(originalBitmap.height)

    if (cropWidth > cropHeight){
        startY -= 38
        startX -=23
        validCropWidth += 45
        validCropHeight += 75
    }else{
        startY -= 50
        startX -= 49
        validCropWidth += 105
        validCropHeight += 100
    }

    return Bitmap.createBitmap(originalBitmap, startX, startY, validCropWidth, validCropHeight).asImageBitmap()
}