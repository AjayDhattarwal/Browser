package com.ar.webwiz.data.local.roomdatabase.downloadDb

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromDownloadRangeList(value: List<DownloadRange>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toDownloadRangeList(value: String): List<DownloadRange> {
        return json.decodeFromString(value)
    }
}
