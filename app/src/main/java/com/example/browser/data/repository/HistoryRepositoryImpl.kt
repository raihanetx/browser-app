package com.example.browser.data.repository

import com.example.browser.data.local.dao.HistoryDao
import com.example.browser.data.local.entity.HistoryEntity
import com.example.browser.domain.model.HistoryEntry
import com.example.browser.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getRecentHistory(limit: Int): Flow<List<HistoryEntry>> {
        return historyDao.getRecentHistory(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchHistory(query: String): Flow<List<HistoryEntry>> {
        return historyDao.searchHistory(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun recordVisit(url: String, title: String) {
        historyDao.insertEntry(
            HistoryEntity(url = url, title = title)
        )
    }

    override suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    private fun HistoryEntity.toDomain() = HistoryEntry(
        id = id,
        url = url,
        title = title,
        visitedAt = visitedAt
    )
}
