package com.memoryaid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.memoryaid.databinding.ItemMemoryBinding
import java.util.*

class MemoryAdapter(
    private val memories: List<Memory>,
    private val onMemoryClick: (Memory) -> Unit
) : RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder>() {

    inner class MemoryViewHolder(private val binding: ItemMemoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var tts: TextToSpeech? = null

        fun bind(memory: Memory) {
            binding.title.text = memory.title
            binding.description.text = memory.description

            if (!memory.mediaUri.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(memory.mediaUri)
                    val isVideo = uri.toString().contains("video") || uri.toString().endsWith(".mp4")

                    binding.mediaThumb.setImageURI(uri)
                    binding.mediaThumb.visibility = View.VISIBLE

                    binding.mediaThumb.setOnClickListener {
                        if (isVideo) {
                            val context = binding.root.context
                            val intent = Intent(context, VideoPlayerActivity::class.java)
                            intent.putExtra("videoUri", uri.toString())
                            context.startActivity(intent)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.mediaThumb.visibility = View.GONE
                    Toast.makeText(binding.root.context, "Invalid media", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.mediaThumb.visibility = View.GONE
            }

            // Text-to-Speech init
            if (tts == null) {
                tts = TextToSpeech(binding.root.context) { status ->
                    if (status != TextToSpeech.ERROR) {
                        tts?.language = Locale.getDefault()
                    }
                }
            }

            binding.speakButton.setOnClickListener {
                val toSpeak = "Title: ${memory.title}. Description: ${memory.description}"
                tts?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }

            binding.root.setOnClickListener {
                onMemoryClick(memory)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val binding = ItemMemoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        holder.bind(memories[position])
    }

    override fun getItemCount(): Int = memories.size
}

