package com.ar.idm.data.local.roomdatabase.tabstate

import androidx.room.*
import com.ar.idm.domain.model.roommodel.TabEntity

@Dao
interface TabDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity)

    @Upsert
    suspend fun upsertTab(tabEntity: TabEntity)

    @Query("DELETE FROM tab_table WHERE webViewId = :webViewId")
    suspend fun delete(webViewId: String)

    @Query("SELECT * FROM tab_table ORDER BY `index` DESC")
    suspend fun getAllTabs(): List<TabEntity>

    @Query("SELECT * FROM tab_table WHERE webViewId = :webViewId LIMIT 1")
    suspend fun getTabByWebViewId(webViewId: String): TabEntity?

}
