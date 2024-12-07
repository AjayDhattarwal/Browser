package com.ar.webwiz.data.local.roomdatabase.bookmarkDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Bookmark::class], version = 1, exportSchema = false)
abstract class BookmarkDatabase: RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var Instance: BookmarkDatabase? = null


        fun getDatabase(context: Context): BookmarkDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    BookmarkDatabase::class.java,
                    "bookmark_database"
                ).build().also {
                    Instance = it
                }
            }
        }

    }



}