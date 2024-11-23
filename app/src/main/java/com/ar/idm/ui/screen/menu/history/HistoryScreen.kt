@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.ar.idm.ui.screen.menu.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ar.idm.ui.components.DropDown
import com.ar.idm.ui.components.TopBar
import com.ar.idm.ui.navigation.AppDestination
import com.ar.idm.ui.components.AlertDialog
import com.ar.idm.ui.screen.menu.history.components.HistoryItem
import com.ar.idm.viewmodel.HistoryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigate: (AppDestination) -> Unit,
){
    val historyState by historyViewModel.historyState.collectAsState()
    var dropDownsState by remember { mutableStateOf(false) }
    var clearHistoryDialogState by remember { mutableStateOf(false) }

    val dropDownList = listOf(
        Pair( { clearHistoryDialogState = true }, "Clear All"),
        Pair( { navigate(AppDestination.Settings) }, "Settings"),
    )

    Scaffold(
        modifier = Modifier
            .testTag("History")
            .fillMaxSize(),
        topBar = {
            TopBar(
                title = "History",
                navigateBack = navigateBack,
                menuAction = {
                    dropDownsState = !dropDownsState

                }
            ){
                DropDown(
                    expanded = dropDownsState,
                    list = dropDownList
                ){
                    dropDownsState = false
                }
            }

        },
        containerColor =  MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(it).fillMaxSize()){
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp)
            ) {
                historyState.forEach { pair ->
                    stickyHeader(key = {pair.first}) {
                        Box(
                            modifier = Modifier.fillMaxWidth().background(
                                color = MaterialTheme.colorScheme.surface
                            )
                        ){
                            Text(
                                text = pair.first,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    items(pair.second, key = {item-> item.id}){ item->
                        HistoryItem(
                            modifier = Modifier.animateItem(),
                            image = item.imageUrl,
                            title = item.title,
                            subtitle = item.url,
                            onClear = { historyViewModel.deleteHistory(item.id)},
                            onClick = {}
                        )
                    }
                }
            }
        }
    }

    if(clearHistoryDialogState){
        dropDownsState = false

        AlertDialog(
            onDismissRequest = { clearHistoryDialogState = false },
            buttonTitle = "Clear History",
            onConfirm = {
                clearHistoryDialogState = false
                historyViewModel.clearAllHistory()
            },
            content = { Text("Are you sure you want to clear all history?") },
        )
    }
}