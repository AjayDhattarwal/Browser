package com.ar.webwiz.data.local.roomdatabase.downloadDb

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "download_files",
    indices = [Index(value = ["uuid"], unique = true)]
)
data class DownloadFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val downloadUrl: String,
    val fileSize: Long,
    val downloadLocation: String,
    val fileType: String,
    val isPauseAble: Boolean,
    val pausedRanges: List<DownloadRange>? = null,
    val notificationId: Int,
    val uuid: String,
    val currentDownloadedSize: Long,
    val time: Long
)

@Serializable
data class DownloadRange(
    val newStart: Long,
    val end: Long,
    val threadIndex: Int,
    val initialStart: Long
)