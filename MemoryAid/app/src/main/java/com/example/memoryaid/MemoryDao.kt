package com.memoryaid

import androidx.room.*

@Dao
interface MemoryDao {
    @Insert
    suspend fun insert(memory: MemoryEntity)

    @Query("SELECT * FROM memories WHERE tagId = :tagId")
    suspend fun getMemoriesByTag(tagId: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    suspend fun getMemoryById(id: Int): MemoryEntity?

    @Update
    suspend fun update(memory: MemoryEntity)

    @Delete
    suspend fun delete(memory: MemoryEntity)
}
