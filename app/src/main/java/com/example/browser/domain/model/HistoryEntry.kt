package com.example.browser.domain.model

data class HistoryEntry(
    val id: Long = 0,
    val url: String,
    val title: String,
    val visitedAt: Long = System.currentTimeMillis()
)
