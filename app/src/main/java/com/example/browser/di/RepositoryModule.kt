package com.example.browser.di

import com.example.browser.data.repository.BookmarkRepositoryImpl
import com.example.browser.data.repository.HistoryRepositoryImpl
import com.example.browser.data.repository.TabRepositoryImpl
import com.example.browser.domain.repository.BookmarkRepository
import com.example.browser.domain.repository.HistoryRepository
import com.example.browser.domain.repository.TabRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindTabRepository(impl: TabRepositoryImpl): TabRepository
}
