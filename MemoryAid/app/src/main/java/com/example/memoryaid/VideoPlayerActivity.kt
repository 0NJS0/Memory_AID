package com.memoryaid

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.memoryaid.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUri = intent.getStringExtra("videoUri")
        if (videoUri == null) {
            Toast.makeText(this, "Invalid video", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val uri = Uri.parse(videoUri)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)

        binding.videoView.setVideoURI(uri)
        binding.videoView.setOnPreparedListener {
            binding.videoView.start()
        }

        binding.videoView.setOnErrorListener { _, what, extra ->
            Toast.makeText(this, "Can't play this video", Toast.LENGTH_LONG).show()
            true
        }
    }
}
