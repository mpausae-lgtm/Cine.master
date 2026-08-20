package com.example.data

import kotlinx.coroutines.flow.Flow

class EditRepository(private val editDao: EditDao) {
    val allProjects: Flow<List<EditProjectEntity>> = editDao.getAllProjects()

    suspend fun insertProject(project: EditProjectEntity) {
        editDao.insertProject(project)
    }

    suspend fun deleteProject(id: Long) {
        editDao.deleteProjectById(id)
    }
}
