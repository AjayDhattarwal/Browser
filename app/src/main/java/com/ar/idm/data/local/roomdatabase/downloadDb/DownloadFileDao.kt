package com.ar.idm.data.local.roomdatabase.downloadDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DownloadFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(downloadFile: DownloadFileEntity)

    @Query("SELECT * FROM download_files")
    suspend fun getAllDownloadFiles(): List<DownloadFileEntity>

    @Query("SELECT * FROM download_files WHERE uuid = :uuid")
    suspend fun getDownloadFileByUuid(uuid: String): DownloadFileEntity?

    @Query("DELETE FROM download_files WHERE uuid = :uuid")
    suspend fun deleteDownloadingFile(uuid: String)

}
