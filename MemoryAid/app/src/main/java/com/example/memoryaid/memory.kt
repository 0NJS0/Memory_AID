package com.memoryaid

data class Memory(
    val id: Long,
    val tagId: String,
    val title: String,
    val description: String,
    val mediaUri: String? = null
)
