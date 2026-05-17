package com.example.browser.presentation.main

/**
 * Single source of truth for the browser UI state.
 * Immutable — ViewModel emits new copies, UI observes.
 */
data class BrowserUiState(
    val currentUrl: String = "",
    val currentTitle: String = "",
    val isLoading: Boolean = false,
    val loadProgress: Int = 0,
    val isDesktopMode: Boolean = false,
    val isBookmarked: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val tabCount: Int = 1,
    val addressBarText: String = "",
    val isAddressBarFocused: Boolean = false
)
