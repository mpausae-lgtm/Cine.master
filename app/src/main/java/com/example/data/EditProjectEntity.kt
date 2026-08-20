package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edit_projects")
data class EditProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val prompt: String,
    val intentSummary: String,
    val jsonOutput: String,
    val timestamp: Long = System.currentTimeMillis()
)
