package com.ar.webwiz.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CommonCardRowFrame(
    modifier: Modifier = Modifier,
    contentHorizontalPadding: Dp = 16.dp,
    content: @Composable () -> Unit = {},
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    shape: RoundedCornerShape = RoundedCornerShape(30),
){
    Card (
        modifier = modifier
            .fillMaxWidth(),
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = contentHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon()
            Box(
                modifier = Modifier.weight(1f),
            ){
                content()
            }
            trailingIcon()
        }
    }
}



@Composable
fun CommonCardRowFrame(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    subTitle: String? = null,
    subTitleStyle: TextStyle = MaterialTheme.typography.labelSmall,
    contentHorizontalPadding: Dp = 12.dp,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    shape: Shape = MaterialTheme.shapes.large,
    onClick: () -> Unit = {},
){
    Card (
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = shape,
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = contentHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon()
            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            trailingIcon()
        }
    }
}