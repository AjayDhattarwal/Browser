package com.ar.webwiz.ui.screen.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ar.webwiz.domain.model.BrowserState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun LoadingBar(
    browserState: StateFlow<BrowserState>,
    visible: () -> Boolean
){
    val state by browserState.collectAsState()


    if(state.isLoading && visible()){
        LinearProgressIndicator(
            progress = { state.loadingPercentage },
            trackColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
        )
    }
}