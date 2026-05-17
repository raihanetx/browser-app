package com.example.browser.domain.repository

import com.example.browser.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getRecentHistory(limit: Int = 50): Flow<List<HistoryEntry>>
    fun searchHistory(query: String): Flow<List<HistoryEntry>>
    suspend fun recordVisit(url: String, title: String)
    suspend fun clearHistory()
}
