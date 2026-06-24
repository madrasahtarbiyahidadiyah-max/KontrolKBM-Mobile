package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY timestamp DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity): Long

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteInspectionById(id: Int)

    @Query("DELETE FROM inspections")
    suspend fun clearAllInspections()

    @Query("SELECT * FROM inspections WHERE isSynced = 0 ORDER BY timestamp DESC")
    suspend fun getUnsyncedInspections(): List<InspectionEntity>

    @Query("UPDATE inspections SET isSynced = :isSynced WHERE id = :id")
    suspend fun updateInspectionSyncStatus(id: Int, isSynced: Boolean)
}
