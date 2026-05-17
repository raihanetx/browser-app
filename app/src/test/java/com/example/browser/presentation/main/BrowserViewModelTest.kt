package com.example.browser.presentation.main

import com.example.browser.domain.model.Bookmark
import com.example.browser.domain.repository.BookmarkRepository
import com.example.browser.domain.repository.TabRepository
import com.example.browser.domain.usecase.RecordVisitUseCase
import com.example.browser.util.UrlParser
import com.example.browser.util.UserAgentProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var urlParser: UrlParser
    private lateinit var userAgentProvider: UserAgentProvider
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var tabRepository: TabRepository
    private lateinit var recordVisitUseCase: RecordVisitUseCase
    private lateinit var viewModel: BrowserViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        urlParser = UrlParser()
        userAgentProvider = UserAgentProvider()
        bookmarkRepository = mock()
        tabRepository = mock()
        recordVisitUseCase = mock()

        whenever(bookmarkRepository.isBookmarked(any())).thenReturn(flowOf(false))
        whenever(tabRepository.getTabCount()).thenReturn(flowOf(1))

        viewModel = BrowserViewModel(
            urlParser = urlParser,
            userAgentProvider = userAgentProvider,
            bookmarkRepository = bookmarkRepository,
            tabRepository = tabRepository,
            recordVisitUseCase = recordVisitUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertEquals("", state.currentUrl)
        assertEquals("", state.currentTitle)
        assertFalse(state.isLoading)
        assertEquals(0, state.loadProgress)
        assertFalse(state.isDesktopMode)
        assertFalse(state.isBookmarked)
        assertFalse(state.canGoBack)
        assertFalse(state.canGoForward)
        assertEquals(1, state.tabCount)
    }

    @Test
    fun `loadUrl updates state with parsed URL`() {
        viewModel.loadUrl("google.com")

        val state = viewModel.uiState.value
        assertEquals("https://google.com", state.currentUrl)
        assertTrue(state.isLoading)
    }

    @Test
    fun `loadUrl with search query sets google search URL`() {
        viewModel.loadUrl("hello world")

        val state = viewModel.uiState.value
        assertEquals("https://www.google.com/search?q=hello%20world", state.currentUrl)
    }

    @Test
    fun `onWebViewPageFinished updates loading state`() {
        viewModel.onWebViewPageFinished("https://example.com", "Example")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(100, state.loadProgress)
        assertEquals("Example", state.currentTitle)
    }

    @Test
    fun `onProgressChanged updates progress`() {
        viewModel.onProgressChanged(50)

        assertEquals(50, viewModel.uiState.value.loadProgress)
    }

    @Test
    fun `toggleDesktopMode updates state`() {
        viewModel.toggleDesktopMode(true)
        assertTrue(viewModel.uiState.value.isDesktopMode)

        viewModel.toggleDesktopMode(false)
        assertFalse(viewModel.uiState.value.isDesktopMode)
    }

    @Test
    fun `updateNavigationState updates back and forward`() {
        viewModel.updateNavigationState(canGoBack = true, canGoForward = false)

        val state = viewModel.uiState.value
        assertTrue(state.canGoBack)
        assertFalse(state.canGoForward)
    }

    @Test
    fun `onAddressBarTextChanged updates address bar`() {
        viewModel.onAddressBarTextChanged("https://test.com")
        assertEquals("https://test.com", viewModel.uiState.value.addressBarText)
    }

    @Test
    fun `onAddressBarFocused updates focus state`() {
        viewModel.onAddressBarFocused(true)
        assertTrue(viewModel.uiState.value.isAddressBarFocused)

        viewModel.onAddressBarFocused(false)
        assertFalse(viewModel.uiState.value.isAddressBarFocused)
    }

    @Test
    fun `toggleBookmark adds bookmark when not bookmarked`() = runTest {
        whenever(bookmarkRepository.isBookmarked("https://example.com")).thenReturn(flowOf(false))

        viewModel.loadUrl("https://example.com")
        viewModel.onWebViewPageFinished("https://example.com", "Example")
        advanceUntilIdle()

        viewModel.toggleBookmark()
        advanceUntilIdle()

        verify(bookmarkRepository).addBookmark("https://example.com", "Example")
    }

    @Test
    fun `captureMobileUserAgent stores default agent`() {
        viewModel.captureMobileUserAgent("TestAgent/1.0")
        assertEquals("TestAgent/1.0", userAgentProvider.mobileUserAgent)
    }

    @Test
    fun `getUserAgentForCurrentMode returns desktop when enabled`() {
        userAgentProvider.mobileUserAgent = "MobileAgent"
        viewModel.toggleDesktopMode(true)

        val ua = viewModel.getUserAgentForCurrentMode()
        assertEquals(userAgentProvider.desktopUserAgent, ua)
    }

    @Test
    fun `getUserAgentForCurrentMode returns mobile when disabled`() {
        userAgentProvider.mobileUserAgent = "MobileAgent"
        viewModel.toggleDesktopMode(false)

        val ua = viewModel.getUserAgentForCurrentMode()
        assertEquals("MobileAgent", ua)
    }

    @Test
    fun `onWebViewPageStarted records history`() = runTest {
        viewModel.onWebViewPageFinished("https://example.com", "Example")
        advanceUntilIdle()

        verify(recordVisitUseCase).invoke("https://example.com", "Example")
    }

    @Test
    fun `blank URL does not record history`() = runTest {
        viewModel.onWebViewPageFinished(null, null)
        advanceUntilIdle()

        verify(recordVisitUseCase, never()).invoke(any(), any())
    }
}
