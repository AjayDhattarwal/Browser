package com.ar.webwiz.data.local.roomdatabase.bookmarkDb

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmark",
    indices = [Index(value = ["url"], unique = true)]
)
data class Bookmark (
    @PrimaryKey(autoGenerate = true) val id : Long = 0,
    val url : String,
    val favIcon : String,
    val title : String,
    val timeStamp : Long
)