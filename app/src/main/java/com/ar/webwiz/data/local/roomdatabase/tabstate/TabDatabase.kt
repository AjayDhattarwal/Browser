package com.ar.webwiz.data.local.roomdatabase.tabstate

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ar.webwiz.domain.model.roommodel.TabEntity

@Database(entities = [TabEntity::class], version = 1)
abstract class TabDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao

    companion object {
        @Volatile
        private var INSTANCE: TabDatabase? = null

        fun getDatabase(context: Context): TabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TabDatabase::class.java,
                    "tab_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}