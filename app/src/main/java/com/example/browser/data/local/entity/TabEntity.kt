package com.example.browser.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val isDesktopMode: Boolean = false,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
