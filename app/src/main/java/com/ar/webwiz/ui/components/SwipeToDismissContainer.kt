package com.ar.webwiz.ui.components

import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun <T> SwipeToDeleteContainer(
    modifier: Modifier = Modifier,
    item: T,
    onRemove: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    val density = LocalDensity.current

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                onRemove(item)
                true
            }
            else{
                false
            }
        },
        positionalThreshold = { 150.dp.toPx(density) }
    )



    SwipeToDismissBox(
        modifier = modifier,
        state = state,
        backgroundContent = {}
    ) {
        content(item)
    }

}






fun Dp.toPx(density: Density): Float {
    return with(density){this@toPx.toPx()}
}