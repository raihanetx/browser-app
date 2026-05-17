package com.example.browser.domain.repository

import com.example.browser.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getAllBookmarks(): Flow<List<Bookmark>>
    fun isBookmarked(url: String): Flow<Boolean>
    suspend fun addBookmark(url: String, title: String)
    suspend fun removeBookmark(url: String)
}
