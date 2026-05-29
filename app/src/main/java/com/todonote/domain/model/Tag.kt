package com.todonote.domain.model

data class Tag(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val createdAt: Long = System.currentTimeMillis()
)
