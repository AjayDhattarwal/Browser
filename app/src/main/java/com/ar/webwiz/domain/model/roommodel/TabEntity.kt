package com.ar.webwiz.domain.model.roommodel

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tab_table",
    indices = [Index(value = ["webViewId"], unique = true)]
)
data class TabEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val index: Int,
    val webViewId: String,
    val overlay: Boolean,
    val title: String,
    val stateFilePath: String? = null,
    val thumbnail: String? = null

)