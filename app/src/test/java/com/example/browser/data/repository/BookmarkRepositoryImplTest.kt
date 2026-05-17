package com.example.browser.data.repository

import com.example.browser.data.local.dao.BookmarkDao
import com.example.browser.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BookmarkRepositoryImplTest {

    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var repository: BookmarkRepositoryImpl

    @Before
    fun setup() {
        bookmarkDao = mock()
        repository = BookmarkRepositoryImpl(bookmarkDao)
    }

    @Test
    fun `getAllBookmarks maps entities to domain models`() = runTest {
        val entities = listOf(
            BookmarkEntity(id = 1, url = "https://a.com", title = "A"),
            BookmarkEntity(id = 2, url = "https://b.com", title = "B")
        )
        whenever(bookmarkDao.getAllBookmarks()).thenReturn(flowOf(entities))

        val bookmarks = repository.getAllBookmarks().first()

        assertEquals(2, bookmarks.size)
        assertEquals("https://a.com", bookmarks[0].url)
        assertEquals("A", bookmarks[0].title)
        assertEquals("https://b.com", bookmarks[1].url)
    }

    @Test
    fun `isBookmarked delegates to dao`() = runTest {
        whenever(bookmarkDao.isBookmarked("https://test.com")).thenReturn(flowOf(true))

        val result = repository.isBookmarked("https://test.com").first()

        assertTrue(result)
    }

    @Test
    fun `isBookmarked returns false when not found`() = runTest {
        whenever(bookmarkDao.isBookmarked("https://unknown.com")).thenReturn(flowOf(false))

        val result = repository.isBookmarked("https://unknown.com").first()

        assertFalse(result)
    }

    @Test
    fun `addBookmark inserts entity via dao`() = runTest {
        repository.addBookmark("https://test.com", "Test")

        verify(bookmarkDao).insertBookmark(
            BookmarkEntity(url = "https://test.com", title = "Test")
        )
    }

    @Test
    fun `removeBookmark deletes by url via dao`() = runTest {
        repository.removeBookmark("https://test.com")

        verify(bookmarkDao).deleteBookmarkByUrl("https://test.com")
    }
}
