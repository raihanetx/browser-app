package com.example.browser.domain.repository

import com.example.browser.domain.model.BrowserTab
import kotlinx.coroutines.flow.Flow

interface TabRepository {
    fun getAllTabs(): Flow<List<BrowserTab>>
    fun getTabCount(): Flow<Int>
    suspend fun saveTab(tab: BrowserTab)
    suspend fun deleteTab(tabId: String)
    suspend fun deleteAllTabs()
}
