package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EditDao {
    @Query("SELECT * FROM edit_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<EditProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: EditProjectEntity)

    @Query("DELETE FROM edit_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}
