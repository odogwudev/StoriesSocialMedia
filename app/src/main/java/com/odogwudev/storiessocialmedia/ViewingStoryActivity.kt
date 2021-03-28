package com.odogwudev.storiessocialmedia


import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request


class ViewingStoryActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewing_story)

        val webView: WebView = findViewById(R.id.webview)
        val message = intent.getStringExtra("archive_id")
        message?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val deferredVideoUrl = async { getVideoUrl(it) }
                val videoUrl = deferredVideoUrl.await()
                withContext(Dispatchers.Main) {
                    webView.loadUrl(videoUrl)
                }
            }
        }
    }

    suspend fun getVideoUrl(archiveId: String): String {
        var request = Request.Builder().url("${getString(R.string.SERVER)}/stories/videos/${archiveId}").build()
        client.newCall(request).execute().use { response ->
            return response.body!!.string()
        }
    }
}