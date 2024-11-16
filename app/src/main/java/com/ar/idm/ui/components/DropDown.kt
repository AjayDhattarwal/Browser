package com.ar.idm.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DropDown(
    expanded: Boolean,
    list : List<Pair<() -> Unit, String>>,
    onDismissRequest: () -> Unit
){

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        list.forEach { value ->
            DropdownMenuItem(
                text = {
                    Text(text = value.second)
                },
                onClick = value.first
            )
        }
    }
}