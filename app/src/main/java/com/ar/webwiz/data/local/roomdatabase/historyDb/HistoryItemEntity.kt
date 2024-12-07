package com.ar.webwiz.data.local.roomdatabase.historyDb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItemEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUrl: String,
    val url: String,
    val title: String,
    val timestamp: Long
)