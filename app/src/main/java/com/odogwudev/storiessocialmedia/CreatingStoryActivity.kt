package com.odogwudev.storiessocialmedia

import android.Manifest
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.opentok.android.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class CreatingStoryActivity : AppCompatActivity(), Session.SessionListener, PublisherKit.PublisherListener  {

    private var mPublisherViewContainer: FrameLayout? = null
    private var mPublisher: Publisher? = null

    private val client = OkHttpClient()

    companion object {
        private val LOG_TAG = "android-stories"
        const val RC_VIDEO_APP_PERM = 124
        private var mSession: Session? = null
    }

    private var token: String? = null
    private var apiKey: String? = null
    private var sessionId: String? = null
    private var archiveId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creating_story)

        val message = intent.getStringArrayExtra("token")
        message?.let {
            apiKey = it[0]
            token = it[1]
            sessionId = it[2]

            requestPermissions()
        }

        val button = findViewById<Button>(R.id.publishbutton)
        button.setOnClickListener {
            mSession!!.unpublish(mPublisher)
            CoroutineScope(Dispatchers.IO).launch {
                val deferredStopArchive = async { stopArchive() }
                deferredStopArchive.await()
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private fun requestPermissions() {
        val perms = arrayOf<String>(Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            mPublisherViewContainer = findViewById(R.id.publisher)

            mSession = Session.Builder(this, this.apiKey, this.sessionId).build()
            mSession?.let {
                it.setSessionListener(this)
                it.connect(this.token)
            }

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, *perms)
        }
    }

    suspend fun startArchive(): Unit {
        var request = Request.Builder().url("${getString(R.string.SERVER)}/stories/video-start-archive/${sessionId}").build()
        client.newCall(request).execute().use { response ->
            val string = response.body!!.string()
            archiveId = string
        }
    }

    suspend fun stopArchive(): Unit {
        var request = Request.Builder().url("${getString(R.string.SERVER)}/stories/video-stop-archive/${archiveId}").build()
        client.newCall(request).execute()
    }

    override fun onConnected(session: Session?) {
        Log.i(LOG_TAG, "Session Connected")

        mPublisher = Publisher.Builder(this).build()
        mPublisher?.let {
            it.setPublisherListener(this)
            it.renderer.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)

            mPublisherViewContainer!!.addView(it.view)

            if (it.view is GLSurfaceView) {
                (it.view as GLSurfaceView).setZOrderOnTop(true)
            }

            mSession!!.publish(mPublisher)

            CoroutineScope(Dispatchers.IO).launch {
                val deferredStartArchive = async { startArchive() }
                deferredStartArchive.await()
            }
        }

    }

    override fun onDisconnected(session: Session?) {
        Log.i(LOG_TAG, "Session Disconnected")
    }

    override fun onStreamReceived(session: Session?, stream: Stream?) {
        Log.i(LOG_TAG, "Stream Received")
    }

    override fun onStreamDropped(session: Session?, stream: Stream?) {
        Log.i(LOG_TAG, "Stream Dropped")
    }

    override fun onError(publisherKit: Session?, opentokError: OpentokError?) {
        opentokError?.let {
            Log.e(LOG_TAG, "Session error: " + opentokError.getMessage())
        }
    }

    override fun onError(publisherKit: PublisherKit?, opentokError: OpentokError?) {
        opentokError?.let {
            Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage())
        }
    }

    override fun onStreamCreated(publisherKit: PublisherKit?, stream: Stream?) {
        Log.i(LOG_TAG, "Publisher onStreamCreated")
    }

    override fun onStreamDestroyed(publisherKit: PublisherKit?, stream: Stream?) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed")
    }
}