package com.memoryaid

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tagId: String,
    val title: String,
    val description: String,
    val mediaUri: String? = null // ✅ added for image/video
)
