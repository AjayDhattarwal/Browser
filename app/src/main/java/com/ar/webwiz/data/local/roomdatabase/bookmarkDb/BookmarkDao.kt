package com.ar.webwiz.data.local.roomdatabase.bookmarkDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Update
    suspend fun updateBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("SELECT * from bookmark ORDER BY timeStamp DESC ")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * from bookmark WHERE url = :url")
    suspend fun getBookmarkByUrl(url: String): Bookmark?

    @Query("SELECT * FROM bookmark WHERE strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch') = :date LIMIT 1")
    suspend fun getBookmarkByDate(date: String): Bookmark

    @Query("DELETE FROM bookmark")
    suspend fun clearAllBookmarks()

    @Query("DELETE FROM bookmark WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)


}