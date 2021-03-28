package com.odogwudev.storiessocialmedia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.opentok.android.*
import okhttp3.*
import kotlinx.coroutines.*

const val REQUEST_CODE_CREATE_STORY = 1
const val REQUEST_CODE_VIEW_STORY = 2


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val client = OkHttpClient()

    private var videosMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val self = this
            CoroutineScope(Dispatchers.IO).launch {
                val deferredToken = async { getToken() }
                val results = deferredToken.await()

                withContext(Dispatchers.Main) {
                    val intent = Intent(self, CreatingStoryActivity::class.java).apply {
                        putExtra("token", results)
                    }
                    startActivityForResult(intent, REQUEST_CODE_CREATE_STORY)
                }
            }
        }

        loadUpVideos()

    }

    fun loadUpVideos() {
        val self = this
        viewManager = LinearLayoutManager(this)
        CoroutineScope(Dispatchers.IO).launch {
            val deferredVideos = async { getVideos() }
            val videosList = deferredVideos.await()
            videosMap = mutableMapOf<String, String>()
            for (video in videosList) {
                videosMap.put(video.name, video.archive_id)
            }
            viewAdapter = StoryAdapter(videosList) { view: View ->
                val button: Button = view as Button
                CoroutineScope(Dispatchers.IO).launch {
                    val archiveId = videosMap[button.text.toString()]
                    withContext(Dispatchers.Main) {
                        val intent = Intent(self, ViewingStoryActivity::class.java).apply {
                            putExtra("archive_id", archiveId)
                        }
                        startActivityForResult(intent, REQUEST_CODE_VIEW_STORY)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                recyclerView = findViewById<RecyclerView>(R.id.listview).apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode== REQUEST_CODE_CREATE_STORY) {
            loadUpVideos()
        }
    }

    suspend fun getToken(): Array<String> {
        var request = Request.Builder().url("${getString(R.string.SERVER)}/stories/token").build()
        client.newCall(request).execute().use { response ->
            val string = response.body!!.string()
            val gson = Gson()
            val tokenJson = gson.fromJson(string, TokenJson::class.java)
            val session_id = tokenJson.session
            val token = tokenJson.token
            val api_key = tokenJson.api_key
            return arrayOf<String>(api_key, token, session_id)
        }
    }

    suspend fun getVideos(): Array<VideoJson> {
        var request = Request.Builder().url("${getString(R.string.SERVER)}/stories/videos-list").build()
        client.newCall(request).execute().use { response ->
            val string = response.body!!.string()
            val gson = Gson()
            val videosJson = gson.fromJson(string, Array<VideoJson>::class.java)
            return videosJson
        }
    }

}

class TokenJson(
    val token: String,
    val session: String,
    val api_key: String
)

class VideoJson(
    val name: String,
    val archive_id: String
)