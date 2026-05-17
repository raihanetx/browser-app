package com.example.browser.util

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAgentProvider @Inject constructor() {

    val desktopUserAgent: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    // Mobile UA is read from WebView settings at runtime — not hardcoded
    // Store it once, then switch between the two
    var mobileUserAgent: String = ""
}
