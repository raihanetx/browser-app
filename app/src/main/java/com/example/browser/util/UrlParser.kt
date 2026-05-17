package com.example.browser.util

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlParser @Inject constructor() {

    fun parseInput(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.isBlank() -> "about:blank"
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") &&
                !trimmed.startsWith(".") && !trimmed.endsWith(".") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${Uri.encode(trimmed)}"
        }
    }

    fun extractDomain(url: String): String {
        return try {
            Uri.parse(url).host ?: url
        } catch (e: Exception) {
            url
        }
    }

    fun isValidUrl(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            uri.scheme != null && uri.host != null
        } catch (e: Exception) {
            false
        }
    }
}
