package com.example.browser.data.repository

import com.example.browser.data.local.dao.TabDao
import com.example.browser.data.local.entity.TabEntity
import com.example.browser.domain.model.BrowserTab
import com.example.browser.domain.repository.TabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TabRepositoryImpl @Inject constructor(
    private val tabDao: TabDao
) : TabRepository {

    override fun getAllTabs(): Flow<List<BrowserTab>> {
        return tabDao.getAllTabs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTabCount(): Flow<Int> {
        return tabDao.getTabCount()
    }

    override suspend fun saveTab(tab: BrowserTab) {
        tabDao.upsertTab(tab.toEntity())
    }

    override suspend fun deleteTab(tabId: String) {
        tabDao.deleteTab(tabId)
    }

    override suspend fun deleteAllTabs() {
        tabDao.deleteAllTabs()
    }

    private fun TabEntity.toDomain() = BrowserTab(
        id = id,
        url = url,
        title = title,
        isDesktopMode = isDesktopMode
    )

    private fun BrowserTab.toEntity() = TabEntity(
        id = id,
        url = url,
        title = title,
        isDesktopMode = isDesktopMode,
        position = 0
    )
}
