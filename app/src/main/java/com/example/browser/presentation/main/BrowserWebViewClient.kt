package com.example.browser.presentation.main

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebViewClient that delegates all events to the ViewModel via callbacks.
 * No business logic lives here — just event forwarding.
 */
class BrowserWebViewClient(
    private val onPageStarted: ((url: String?) -> Unit)? = null,
    private val onPageFinished: ((url: String?) -> Unit)? = null,
    private val onReceivedError: ((error: String) -> Unit)? = null,
    private val onTitleReceived: ((title: String?) -> Unit)? = null
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted?.invoke(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinished?.invoke(url)
        view?.title?.let { onTitleReceived?.invoke(it) }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            onReceivedError?.invoke(error?.description?.toString() ?: "Unknown error")
        }
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        // Production: show dialog to user. Cancel by default for safety.
        handler?.cancel()
        onReceivedError?.invoke("SSL Error")
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        view?.title?.let { onTitleReceived?.invoke(it) }
    }
}
