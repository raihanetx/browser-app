package com.example.browser.presentation.main

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.browser.R
import com.example.browser.databinding.ActivityMainBinding
import com.example.browser.databinding.LayoutBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private lateinit var sheetBinding: LayoutBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        sheetBinding = LayoutBottomSheetBinding.bind(binding.bottomSheetContent.root)
        setContentView(binding.root)

        setupWebView()
        setupBottomSheet()
        setupSearchBar()
        setupNavigationControls()
        setupFullPanelControls()
        handleBackPress()

        observeUiState()

        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState)
        } else {
            viewModel.loadUrl("https://www.google.com")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    // --- WebView Setup ---

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.allowContentAccess = true
            settings.setSupportMultipleWindows(false)
            settings.mediaPlaybackRequiresUserGesture = false

            // Capture default mobile UA before any switching
            viewModel.captureMobileUserAgent(settings.userAgentString)

            webViewClient = BrowserWebViewClient(
                onPageStarted = { url ->
                    viewModel.onWebViewPageStarted(url)
                },
                onPageFinished = { url ->
                    viewModel.onWebViewPageFinished(url, this.title)
                    viewModel.updateNavigationState(canGoBack(), canGoForward())
                },
                onReceivedError = { error ->
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                },
                onTitleReceived = { title ->
                    viewModel.onTitleReceived(title)
                }
            )

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    viewModel.onProgressChanged(newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    viewModel.onTitleReceived(title)
                }
            }

            setDownloadListener { url, _, _, _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Cannot download", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Bottom Sheet ---

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        val peekHeight = resources.getDimensionPixelSize(R.dimen.peek_height)

        bottomSheetBehavior.apply {
            this.peekHeight = peekHeight
            isHideable = false
            halfExpandedRatio = 0.45f
            isFitToContents = false
            saveFlags = BottomSheetBehavior.SAVE_ALL

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            setTierVisibility(tier1Alpha = 0.7f, tier2Alpha = 0.5f, tier3Alpha = 1f)
                            hideKeyboard()
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            setTierVisibility(tier1Alpha = 0.7f, tier2Alpha = 1f, tier3Alpha = 0f)
                            hideKeyboard()
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            setTierVisibility(tier1Alpha = 1f, tier2Alpha = 0f, tier3Alpha = 0f)
                        }
                        else -> Unit
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    when {
                        slideOffset < 0.3f -> {
                            sheetBinding.tier1Container.alpha = 1f
                            sheetBinding.tier2Container.alpha = 0f
                            sheetBinding.tier3Container.alpha = 0f
                        }
                        slideOffset in 0.3f..0.6f -> {
                            val p = (slideOffset - 0.3f) / 0.3f
                            sheetBinding.tier1Container.alpha = 1f - p * 0.3f
                            sheetBinding.tier2Container.alpha = p
                            sheetBinding.tier3Container.alpha = 0f
                        }
                        else -> {
                            val p = (slideOffset - 0.6f) / 0.4f
                            sheetBinding.tier1Container.alpha = 0.7f
                            sheetBinding.tier2Container.alpha = 1f - p * 0.5f
                            sheetBinding.tier3Container.alpha = p
                        }
                    }
                }
            })
        }

        setTierVisibility(tier1Alpha = 1f, tier2Alpha = 0f, tier3Alpha = 0f)
    }

    private fun setTierVisibility(tier1Alpha: Float, tier2Alpha: Float, tier3Alpha: Float) {
        sheetBinding.tier1Container.alpha = tier1Alpha
        sheetBinding.tier2Container.alpha = tier2Alpha
        sheetBinding.tier3Container.alpha = tier3Alpha
    }

    // --- Search Bar ---

    private fun setupSearchBar() {
        sheetBinding.etAddress.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.onAddressBarSubmitted()
                    hideKeyboard()
                    clearFocus()
                    true
                } else false
            }

            setOnFocusChangeListener { _, hasFocus ->
                viewModel.onAddressBarFocused(hasFocus)
                if (hasFocus) selectAll()
            }
        }

        sheetBinding.btnSearchGo.setOnClickListener {
            viewModel.onAddressBarSubmitted()
            hideKeyboard()
            sheetBinding.etAddress.clearFocus()
        }
    }

    // --- Navigation Controls ---

    private fun setupNavigationControls() {
        sheetBinding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) binding.webView.goBack()
        }
        sheetBinding.btnForward.setOnClickListener {
            if (binding.webView.canGoForward()) binding.webView.goForward()
        }
        sheetBinding.btnRefresh.setOnClickListener {
            binding.webView.reload()
        }
        sheetBinding.btnShare.setOnClickListener {
            val url = binding.webView.url ?: return@setOnClickListener
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                putExtra(Intent.EXTRA_SUBJECT, binding.webView.title ?: "")
            }, "Share via"))
        }
        sheetBinding.btnBookmark.setOnClickListener {
            viewModel.toggleBookmark()
        }
    }

    // --- Full Panel Controls ---

    private fun setupFullPanelControls() {
        sheetBinding.switchDesktopMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleDesktopMode(isChecked)
            val ua = viewModel.getUserAgentForCurrentMode()
            binding.webView.settings.userAgentString = ua
            binding.webView.reload()
            Toast.makeText(
                this,
                if (isChecked) "Desktop mode" else "Mobile mode",
                Toast.LENGTH_SHORT
            ).show()
        }

        sheetBinding.btnOpenExternal.setOnClickListener {
            val url = binding.webView.url ?: return@setOnClickListener
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show()
            }
        }

        sheetBinding.btnClearData.setOnClickListener {
            CookieManager.getInstance().apply {
                removeAllCookies(null)
                flush()
            }
            binding.webView.clearCache(true)
            binding.webView.clearHistory()
            binding.webView.clearFormData()
            viewModel.clearBrowsingData()
            Toast.makeText(this, "Data cleared", Toast.LENGTH_SHORT).show()
        }

        sheetBinding.btnNewTab.setOnClickListener {
            binding.webView.loadUrl("about:blank")
            sheetBinding.etAddress.setText("")
            sheetBinding.etAddress.hint = getString(R.string.search_or_enter_url)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        sheetBinding.btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings — coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Observe ViewModel State ---

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Address bar text (only when not focused)
                launch {
                    viewModel.uiState
                        .map { it.addressBarText }
                        .distinctUntilChanged()
                        .collectLatest { text ->
                            if (!sheetBinding.etAddress.isFocused) {
                                sheetBinding.etAddress.setText(text)
                                sheetBinding.etAddress.setSelection(text.length)
                            }
                        }
                }

                // Navigation buttons
                launch {
                    viewModel.uiState.collectLatest { state ->
                        sheetBinding.btnBack.isEnabled = state.canGoBack
                        sheetBinding.btnBack.alpha = if (state.canGoBack) 1f else 0.3f

                        sheetBinding.btnForward.isEnabled = state.canGoForward
                        sheetBinding.btnForward.alpha = if (state.canGoForward) 1f else 0.3f
                    }
                }

                // Progress bar
                launch {
                    viewModel.uiState.collectLatest { state ->
                        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                        binding.progressBar.progress = state.loadProgress
                    }
                }

                // Page title
                launch {
                    viewModel.uiState
                        .map { it.currentTitle }
                        .distinctUntilChanged()
                        .collectLatest { title ->
                            sheetBinding.tvPageTitle.text = title
                        }
                }

                // Bookmark icon state
                launch {
                    viewModel.uiState
                        .map { it.isBookmarked }
                        .distinctUntilChanged()
                        .collectLatest { bookmarked ->
                            sheetBinding.btnBookmark.setIconResource(
                                if (bookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
                            )
                        }
                }

                // Desktop mode switch (sync if restored)
                launch {
                    viewModel.uiState
                        .map { it.isDesktopMode }
                        .distinctUntilChanged()
                        .collectLatest { desktop ->
                            if (sheetBinding.switchDesktopMode.isChecked != desktop) {
                                sheetBinding.switchDesktopMode.isChecked = desktop
                            }
                            val ua = viewModel.getUserAgentForCurrentMode()
                            binding.webView.settings.userAgentString = ua
                        }
                }

                // Tab counter
                launch {
                    viewModel.uiState
                        .map { it.tabCount }
                        .distinctUntilChanged()
                        .collectLatest { count ->
                            sheetBinding.tvTabCounter.text = count.toString()
                        }
                }

                // Load URL from ViewModel
                launch {
                    viewModel.uiState
                        .map { it.currentUrl }
                        .distinctUntilChanged()
                        .collectLatest { url ->
                            if (url.isNotBlank() && binding.webView.url != url) {
                                binding.webView.loadUrl(url)
                            }
                        }
                }
            }
        }
    }

    // --- Back Press ---

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED ||
                    bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    binding.webView.canGoBack() -> binding.webView.goBack()
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    // --- Lifecycle ---

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(sheetBinding.etAddress.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.webView.apply {
            stopLoading()
            destroy()
        }
        super.onDestroy()
    }
}
