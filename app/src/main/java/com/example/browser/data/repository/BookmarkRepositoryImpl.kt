package com.example.browser.data.repository

import com.example.browser.data.local.dao.BookmarkDao
import com.example.browser.data.local.entity.BookmarkEntity
import com.example.browser.domain.model.Bookmark
import com.example.browser.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isBookmarked(url: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(url)
    }

    override suspend fun addBookmark(url: String, title: String) {
        bookmarkDao.insertBookmark(
            BookmarkEntity(url = url, title = title)
        )
    }

    override suspend fun removeBookmark(url: String) {
        bookmarkDao.deleteBookmarkByUrl(url)
    }

    private fun BookmarkEntity.toDomain() = Bookmark(
        id = id,
        url = url,
        title = title
    )
}
