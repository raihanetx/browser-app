package com.example.browser.data.repository

import com.example.browser.data.local.dao.HistoryDao
import com.example.browser.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HistoryRepositoryImplTest {

    private lateinit var historyDao: HistoryDao
    private lateinit var repository: HistoryRepositoryImpl

    @Before
    fun setup() {
        historyDao = mock()
        repository = HistoryRepositoryImpl(historyDao)
    }

    @Test
    fun `getRecentHistory maps entities to domain models`() = runTest {
        val entities = listOf(
            HistoryEntity(id = 1, url = "https://a.com", title = "A", visitedAt = 100L),
            HistoryEntity(id = 2, url = "https://b.com", title = "B", visitedAt = 200L)
        )
        whenever(historyDao.getRecentHistory(50)).thenReturn(flowOf(entities))

        val history = repository.getRecentHistory(50).first()

        assertEquals(2, history.size)
        assertEquals("https://a.com", history[0].url)
        assertEquals(100L, history[0].visitedAt)
    }

    @Test
    fun `searchHistory delegates to dao with query`() = runTest {
        val entities = listOf(
            HistoryEntity(id = 1, url = "https://google.com", title = "Google", visitedAt = 100L)
        )
        whenever(historyDao.searchHistory("google")).thenReturn(flowOf(entities))

        val results = repository.searchHistory("google").first()

        assertEquals(1, results.size)
        assertEquals("https://google.com", results[0].url)
    }

    @Test
    fun `recordVisit inserts entry via dao`() = runTest {
        repository.recordVisit("https://test.com", "Test")

        verify(historyDao).insertEntry(
            HistoryEntity(url = "https://test.com", title = "Test")
        )
    }

    @Test
    fun `clearHistory delegates to dao`() = runTest {
        repository.clearHistory()

        verify(historyDao).clearHistory()
    }
}
