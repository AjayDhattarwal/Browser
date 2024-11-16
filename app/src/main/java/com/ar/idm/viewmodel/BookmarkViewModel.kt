package com.ar.idm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.idm.data.local.roomdatabase.bookmarkDb.Bookmark
import com.ar.idm.di.viewModelModule
import com.ar.idm.domain.repository.BookmarkRepository
import com.ar.idm.utils.function.extractDomain
import com.ar.idm.utils.function.generateFaviconUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val bookmarkRepo: BookmarkRepository
): ViewModel() {

    private val _bookmarks = MutableStateFlow<List<Pair<String, List<Bookmark>>>>(emptyList())
    val bookmarks = _bookmarks.asStateFlow()



    init{
        getBookmarks()
    }



    private fun getBookmarks(){
        viewModelScope.launch {
            bookmarkRepo.getBookmarks().collectLatest{ bookmark ->
                _bookmarks.value = bookmark
            }
        }
    }

    fun clearAllBookmarks(){
        viewModelScope.launch {
            bookmarkRepo.clearAllBookmarks()
        }
    }

    private suspend fun getBookmarkByUrl(url: String): Bookmark? {
        return bookmarkRepo.getBookmarkByUrl(url)
    }


    fun toggleBookmark(url : String?, title: String?){
        viewModelScope.launch {
            val bookmark = getBookmarkByUrl(url.toString())
            if(bookmark == null){
                insertBookmark(url, title)
            }else{
                deleteBookmark(bookmark)
            }

        }
    }

    fun insertBookmark(url : String?, title: String?) {
        viewModelScope.launch {
            if(url != null){
                val bookmark = Bookmark(
                    url = url,
                    title = title ?: extractDomain(url).toString(),
                    favIcon = generateFaviconUrl(url),
                    timeStamp = System.currentTimeMillis()
                )

                bookmarkRepo.insertBookmark(bookmark)
            }
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkRepo.deleteBookmark(bookmark)
        }
    }

    fun updateBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkRepo.updateBookmark(bookmark)
        }
    }
}