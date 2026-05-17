package com.example.browser.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UrlParserTest {

    private lateinit var urlParser: UrlParser

    @Before
    fun setup() {
        urlParser = UrlParser()
    }

    @Test
    fun `full https URL passes through unchanged`() {
        val result = urlParser.parseInput("https://www.google.com")
        assertEquals("https://www.google.com", result)
    }

    @Test
    fun `full http URL passes through unchanged`() {
        val result = urlParser.parseInput("http://example.com")
        assertEquals("http://example.com", result)
    }

    @Test
    fun `domain without protocol gets https prefix`() {
        val result = urlParser.parseInput("google.com")
        assertEquals("https://google.com", result)
    }

    @Test
    fun `subdomain gets https prefix`() {
        val result = urlParser.parseInput("mail.google.com")
        assertEquals("https://mail.google.com", result)
    }

    @Test
    fun `text without dot becomes google search`() {
        val result = urlParser.parseInput("hello world")
        assertEquals("https://www.google.com/search?q=hello%20world", result)
    }

    @Test
    fun `empty string returns about blank`() {
        val result = urlParser.parseInput("")
        assertEquals("about:blank", result)
    }

    @Test
    fun `blank string returns about blank`() {
        val result = urlParser.parseInput("   ")
        assertEquals("about:blank", result)
    }

    @Test
    fun `text with spaces and dot becomes search`() {
        val result = urlParser.parseInput("hello.world test")
        assertEquals("https://www.google.com/search?q=hello.world%20test", result)
    }

    @Test
    fun `domain starting with dot gets search`() {
        val result = urlParser.parseInput(".com")
        assertEquals("https://www.google.com/search?q=.com", result)
    }

    @Test
    fun `domain ending with dot gets search`() {
        val result = urlParser.parseInput("google.")
        assertEquals("https://www.google.com/search?q=google.", result)
    }

    @Test
    fun `URL with path passes through`() {
        val result = urlParser.parseInput("https://example.com/path/to/page")
        assertEquals("https://example.com/path/to/page", result)
    }

    @Test
    fun `extract domain from full URL`() {
        val domain = urlParser.extractDomain("https://www.google.com/search?q=test")
        assertEquals("www.google.com", domain)
    }

    @Test
    fun `extract domain from invalid URL returns input`() {
        val domain = urlParser.extractDomain("not a url")
        assertEquals("not a url", domain)
    }

    @Test
    fun `valid URL detected correctly`() {
        assertTrue(urlParser.isValidUrl("https://www.google.com"))
    }

    @Test
    fun `invalid URL detected correctly`() {
        assertTrue(!urlParser.isValidUrl("not a url"))
    }
}
