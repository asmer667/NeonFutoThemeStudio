package com.futo.themefuto

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.Base64

class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun saveThemeZip(base64Data: String, fileName: String) {
        try {
            val cleanData = if (base64Data.contains(",")) base64Data.substringAfter(",") else base64Data
            val decodedBytes = Base64.getDecoder().decode(cleanData)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            
            val finalName = if (!fileName.endsWith(".zip")) "$fileName.zip" else fileName
            val file = File(downloadsDir, finalName)
            file.writeBytes(decodedBytes)
            
            (context as? AppCompatActivity)?.runOnUiThread {
                Toast.makeText(context, "تم حفظ الثيم بنجاح في مجلد التنزيلات: ${file.name}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            (context as? AppCompatActivity)?.runOnUiThread {
                Toast.makeText(context, "فشل الحفظ: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/index.html")
    }
}
