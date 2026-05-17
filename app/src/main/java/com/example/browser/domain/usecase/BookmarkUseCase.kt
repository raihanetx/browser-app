package com.example.browser.domain.usecase

import com.example.browser.domain.repository.BookmarkRepository
import com.example.browser.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    fun isBookmarked(url: String): Flow<Boolean> =
        bookmarkRepository.isBookmarked(url)

    suspend fun toggle(url: String, title: String) {
        // Handled in ViewModel — this is a convenience wrapper
    }

    suspend fun add(url: String, title: String) =
        bookmarkRepository.addBookmark(url, title)

    suspend fun remove(url: String) =
        bookmarkRepository.removeBookmark(url)
}
