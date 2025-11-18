package mnhg.dovar

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import java.io.IOException

class MainActivity : ComponentActivity() {

    companion object {
        // Set your site here
        private const val START_URL = "https://emailphone.free.nf/"
    }

    private lateinit var webView: WebView
    private val startHost: String? by lazy {
        Uri.parse(START_URL).host
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true

        // Enable caching and set initial cache mode based on connectivity
        settings.setAppCacheEnabled(true)
        settings.setAppCachePath(cacheDir.absolutePath)
        settings.cacheMode = if (isNetworkAvailable()) {
            WebSettings.LOAD_DEFAULT
        } else {
            WebSettings.LOAD_CACHE_ELSE_NETWORK
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {

            // Intercept requests to provide offline fallback when no network
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val reqUrl = request?.url ?: return super.shouldInterceptRequest(view, request)

                // If offline and request is for our host, try to serve the bundled offline page for HTML navigations
                if (!isNetworkAvailable() && reqUrl.host == startHost) {
                    val path = reqUrl.path ?: "/"
                    if (path == "/" || path.endsWith(".html") || path.isEmpty()) {
                        return try {
                            val input = assets.open("offline.html")
                            WebResourceResponse("text/html", "UTF-8", input)
                        } catch (e: IOException) {
                            null
                        }
                    }
                }

                return super.shouldInterceptRequest(view, request)
            }

            // For API >= 24
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                return handleUri(uri)
            }

            // For older APIs
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url ?: return false
                val uri = Uri.parse(url)
                return handleUri(uri)
            }

            private fun handleUri(uri: Uri): Boolean {
                // If the link is to the same host, let WebView load it.
                if (uri.host == startHost) {
                    return false // WebView will load the URL
                }

                // Otherwise open in external browser
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
                return true
            }
        }

        if (savedInstanceState == null) {
            webView.loadUrl(START_URL)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}