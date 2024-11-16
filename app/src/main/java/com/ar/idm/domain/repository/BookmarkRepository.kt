package com.ar.idm.domain.repository

import com.ar.idm.data.local.roomdatabase.bookmarkDb.BookmarkDatabase
import com.ar.idm.data.local.roomdatabase.bookmarkDb.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookmarkRepository(
    private val bookmarkDatabase: BookmarkDatabase
) {
    private val bookmarkDao = bookmarkDatabase.bookmarkDao()

    fun getBookmarks(): Flow<List<Pair<String, List<Bookmark>>>> {
        return bookmarkDao.getAllBookmarks().map { value ->
            value.groupBy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timeStamp)) }
                .map { it.key to it.value }
                .sortedByDescending { it.first }
        }
    }

    suspend fun insertBookmark(bookmark: Bookmark) {
        withContext(Dispatchers.IO) {
            bookmarkDao.insertBookmark(bookmark)
        }
    }

    suspend fun deleteBookmark(bookmark: Bookmark) {
        withContext(Dispatchers.IO){
            bookmarkDao.deleteBookmark(bookmark)
        }
    }

    suspend fun updateBookmark(bookmark: Bookmark) {
        withContext(Dispatchers.IO) {
            bookmarkDao.updateBookmark(bookmark)
        }
    }

    suspend fun getBookmarkByUrl(url: String): Bookmark? {
        return bookmarkDao.getBookmarkByUrl(url)
    }

    suspend fun getBookmarkByDate(date: String): Bookmark {
        return bookmarkDao.getBookmarkByDate(date)
    }


    suspend fun clearAllBookmarks() {
        withContext(Dispatchers.IO) {
            bookmarkDao.clearAllBookmarks()
        }
    }

    suspend fun deleteBookmarkById(id: Long) {
        withContext(Dispatchers.IO){
            bookmarkDao.deleteBookmarkById(id)
        }
    }


}