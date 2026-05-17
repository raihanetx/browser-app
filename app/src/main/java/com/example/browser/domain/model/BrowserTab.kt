package com.example.browser.domain.model

data class BrowserTab(
    val id: String,
    val url: String,
    val title: String,
    val isDesktopMode: Boolean = false
)
