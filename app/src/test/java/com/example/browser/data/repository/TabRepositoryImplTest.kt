package com.example.browser.data.repository

import com.example.browser.data.local.dao.TabDao
import com.example.browser.data.local.entity.TabEntity
import com.example.browser.domain.model.BrowserTab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TabRepositoryImplTest {

    private lateinit var tabDao: TabDao
    private lateinit var repository: TabRepositoryImpl

    @Before
    fun setup() {
        tabDao = mock()
        repository = TabRepositoryImpl(tabDao)
    }

    @Test
    fun `getAllTabs maps entities to domain models`() = runTest {
        val entities = listOf(
            TabEntity(id = "tab1", url = "https://a.com", title = "A", isDesktopMode = false, position = 0),
            TabEntity(id = "tab2", url = "https://b.com", title = "B", isDesktopMode = true, position = 1)
        )
        whenever(tabDao.getAllTabs()).thenReturn(flowOf(entities))

        val tabs = repository.getAllTabs().first()

        assertEquals(2, tabs.size)
        assertEquals("tab1", tabs[0].id)
        assertEquals("https://a.com", tabs[0].url)
        assertEquals(false, tabs[0].isDesktopMode)
        assertEquals(true, tabs[1].isDesktopMode)
    }

    @Test
    fun `getTabCount delegates to dao`() = runTest {
        whenever(tabDao.getTabCount()).thenReturn(flowOf(3))

        val count = repository.getTabCount().first()

        assertEquals(3, count)
    }

    @Test
    fun `saveTab converts domain model to entity and upserts`() = runTest {
        val tab = BrowserTab(id = "tab1", url = "https://test.com", title = "Test", isDesktopMode = true)

        repository.saveTab(tab)

        verify(tabDao).upsertTab(
            TabEntity(id = "tab1", url = "https://test.com", title = "Test", isDesktopMode = true, position = 0)
        )
    }

    @Test
    fun `deleteTab delegates to dao`() = runTest {
        repository.deleteTab("tab1")

        verify(tabDao).deleteTab("tab1")
    }

    @Test
    fun `deleteAllTabs delegates to dao`() = runTest {
        repository.deleteAllTabs()

        verify(tabDao).deleteAllTabs()
    }
}
