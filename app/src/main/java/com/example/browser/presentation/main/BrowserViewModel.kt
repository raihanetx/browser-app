package com.example.browser.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.browser.domain.repository.BookmarkRepository
import com.example.browser.domain.repository.TabRepository
import com.example.browser.domain.usecase.RecordVisitUseCase
import com.example.browser.util.UrlParser
import com.example.browser.util.UserAgentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val urlParser: UrlParser,
    private val userAgentProvider: UserAgentProvider,
    private val bookmarkRepository: BookmarkRepository,
    private val tabRepository: TabRepository,
    private val recordVisitUseCase: RecordVisitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    // Current tab ID — single tab for v1
    private val currentTabId: String = UUID.randomUUID().toString()

    init {
        observeBookmarkStatus()
        observeTabCount()
    }

    // --- Navigation ---

    fun loadUrl(rawInput: String) {
        val url = urlParser.parseInput(rawInput)
        _uiState.update {
            it.copy(
                currentUrl = url,
                addressBarText = url,
                isLoading = true
            )
        }
        saveCurrentTab()
    }

    fun onWebViewPageStarted(url: String?) {
        url?.let {
            _uiState.update { state ->
                state.copy(
                    currentUrl = it,
                    isLoading = true,
                    loadProgress = 0
                )
            }
            // Update address bar if not focused
            if (!_uiState.value.isAddressBarFocused) {
                _uiState.update { state -> state.copy(addressBarText = it) }
            }
        }
    }

    fun onWebViewPageFinished(url: String?, title: String?) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                loadProgress = 100,
                currentTitle = title ?: "",
                currentUrl = url ?: state.currentUrl
            )
        }
        // Record history visit
        if (url != null && title != null) {
            viewModelScope.launch {
                recordVisitUseCase(url, title)
            }
        }
        // Update address bar if not focused
        if (!_uiState.value.isAddressBarFocused) {
            url?.let {
                _uiState.update { state -> state.copy(addressBarText = it) }
            }
        }
        saveCurrentTab()
    }

    fun onProgressChanged(progress: Int) {
        _uiState.update { it.copy(loadProgress = progress) }
    }

    fun onTitleReceived(title: String?) {
        _uiState.update { it.copy(currentTitle = title ?: "") }
    }

    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    // --- Address Bar ---

    fun onAddressBarTextChanged(text: String) {
        _uiState.update { it.copy(addressBarText = text) }
    }

    fun onAddressBarFocused(focused: Boolean) {
        _uiState.update { it.copy(isAddressBarFocused = focused) }
    }

    fun onAddressBarSubmitted() {
        val input = _uiState.value.addressBarText
        if (input.isNotBlank()) {
            loadUrl(input)
        }
    }

    // --- Desktop Mode ---

    fun toggleDesktopMode(enabled: Boolean) {
        _uiState.update { it.copy(isDesktopMode = enabled) }
        saveCurrentTab()
    }

    fun getUserAgentForCurrentMode(): String {
        return if (_uiState.value.isDesktopMode) {
            userAgentProvider.desktopUserAgent
        } else {
            userAgentProvider.mobileUserAgent
        }
    }

    fun captureMobileUserAgent(defaultAgent: String) {
        if (userAgentProvider.mobileUserAgent.isEmpty()) {
            userAgentProvider.mobileUserAgent = defaultAgent
        }
    }

    // --- Bookmarks ---

    fun toggleBookmark() {
        val state = _uiState.value
        val url = state.currentUrl
        if (url.isBlank()) return

        viewModelScope.launch {
            if (state.isBookmarked) {
                bookmarkRepository.removeBookmark(url)
            } else {
                bookmarkRepository.addBookmark(url, state.currentTitle)
            }
        }
    }

    private fun observeBookmarkStatus() {
        viewModelScope.launch {
            _uiState.collectLatest { state ->
                if (state.currentUrl.isNotBlank()) {
                    bookmarkRepository.isBookmarked(state.currentUrl).collectLatest { bookmarked ->
                        _uiState.update { it.copy(isBookmarked = bookmarked) }
                    }
                }
            }
        }
    }

    // --- Tabs (minimal for v1) ---

    private fun observeTabCount() {
        viewModelScope.launch {
            tabRepository.getTabCount().collectLatest { count ->
                _uiState.update { it.copy(tabCount = maxOf(count, 1)) }
            }
        }
    }

    private fun saveCurrentTab() {
        val state = _uiState.value
        viewModelScope.launch {
            tabRepository.saveTab(
                com.example.browser.domain.model.BrowserTab(
                    id = currentTabId,
                    url = state.currentUrl,
                    title = state.currentTitle,
                    isDesktopMode = state.isDesktopMode
                )
            )
        }
    }

    // --- Clear Data ---

    fun clearBrowsingData() {
        viewModelScope.launch {
            bookmarkRepository // exists for future extension
            // History clearing would go here in v2
        }
    }
}
