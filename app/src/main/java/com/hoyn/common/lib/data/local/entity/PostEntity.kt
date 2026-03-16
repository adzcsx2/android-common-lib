package com.hoyn.common.lib.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    val userId: Int,
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val cachedAt: Long
)