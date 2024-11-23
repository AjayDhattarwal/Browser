package com.ar.idm.ui.screen.menu.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ar.idm.R
import com.ar.idm.data.local.roomdatabase.bookmarkDb.Bookmark
import com.ar.idm.domain.model.BrowserState
import com.ar.idm.ui.components.AlertDialog
import com.ar.idm.ui.components.CommonItemFrame
import com.ar.idm.ui.components.DropDown
import com.ar.idm.ui.components.TopBar
import com.ar.idm.ui.navigation.AppDestination
import com.ar.idm.ui.screen.menu.bookmarks.components.CurrentPageCard
import com.ar.idm.utils.function.generateFaviconUrl
import com.ar.idm.viewmodel.BookmarkViewModel
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookmarkScreen(
    browserState: StateFlow<BrowserState>,
    bookmarkViewModel: BookmarkViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigate: (AppDestination) -> Unit
){
    val state by browserState.collectAsState()
    val groupedBookmarks by bookmarkViewModel.bookmarks.collectAsState()

    val currentTab by remember(state.currentTab) {
        derivedStateOf{
            state.currentTab
        }
    }

    var dropDownsState by remember { mutableStateOf(false) }
    var clearBookmarkDialogState by remember { mutableStateOf(false) }

    val dropDownList = listOf(
        Pair( { clearBookmarkDialogState = true }, "Clear All"),
        Pair( { navigate(AppDestination.Settings) }, "Settings"),
    )
    var selectedBookmark by remember { mutableStateOf<Bookmark?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val isCurrentBookmark by remember {
        derivedStateOf{
            groupedBookmarks.any{
                it.second.any { bookmark ->
                    bookmark.url == state.currentTab?.url
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Bookmark",
                navigateBack = navigateBack,
                menuAction = { dropDownsState = true}
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
    ){
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp)
            ) {
                item(key = "current-page"){
                    CurrentPageCard(
                        modifier = Modifier.padding(12.dp),
                        url = state.currentTab?.url,
                        title = state.currentTab?.title,
                        favIconUrl = state.currentTab?.favIconUrl,
                        isBookmark = { isCurrentBookmark }
                    ){
                        bookmarkViewModel.toggleBookmark(url = currentTab?.url, title = currentTab?.title )
                    }
                }
                groupedBookmarks.forEach { (date, bookmarksForDate) ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface
                                )
                        ) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    items(bookmarksForDate, key = {bookmark -> bookmark.url }) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onEdit = {
                                selectedBookmark = it
                                showEditDialog = true
                            },
                            onDelete = bookmarkViewModel::deleteBookmark,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

            }
        }
    }
    if(clearBookmarkDialogState){
        dropDownsState = false

        AlertDialog(
            onDismissRequest = { clearBookmarkDialogState = false },
            buttonTitle = "Clear Bookmarks",
            onConfirm = {
                bookmarkViewModel.clearAllBookmarks()
                clearBookmarkDialogState = false
            },
            content = { Text("Are you sure you want to clear all Bookmark ?") },
        )
    }

    if (showEditDialog && selectedBookmark != null) {
        BookmarkEditDialog(
            bookmark = selectedBookmark,
            onDismiss = { showEditDialog = false },
            onSave = {
                bookmarkViewModel.updateBookmark(it)
                selectedBookmark = null
                showEditDialog = false
            },
        )
    }

}

@Composable
fun BookmarkEditDialog(
    bookmark: Bookmark? = null,
    onDismiss: () -> Unit,
    onSave: (Bookmark) -> Unit = {},
    onInsert: (url : String, title: String) -> Unit = {_,_ ->}
){
    var title by remember { mutableStateOf(TextFieldValue(bookmark?.title ?: "")) }
    var url by remember { mutableStateOf(TextFieldValue(bookmark?.url ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = {
            if(bookmark != null){
                bookmark.copy(
                    title = title.text,
                    url = url.text,
                    favIcon = generateFaviconUrl(url.text)
                ).let {
                    onSave(
                        it
                    )
                }
            } else{
                onInsert(url.text, title.text)
            }
        },
        buttonTitle = "Save",
    ){
        Column {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun BookmarkScreenDe(
    bookmarks: List<Bookmark>,
    onSearchQueryChange: (String) -> Unit,
    onEditBookmark: (Bookmark) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Search Bar
        var searchQuery by remember { mutableStateOf("") }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchQueryChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search bookmarks...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

    }
}


@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    modifier: Modifier,
    onEdit: (Bookmark) -> Unit,
    onDelete: (Bookmark) -> Unit
) {
    CommonItemFrame(
        modifier = modifier,
        image = bookmark.favIcon,
        title = bookmark.title,
        subtitle = bookmark.url,
        onClick = {}
    ) {
        IconButton(
            onClick = { onEdit(bookmark)},
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit Bookmark"
            )
        }
        IconButton(
            onClick = { onDelete(bookmark)},
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Bookmark"
            )
        }
    }
}



