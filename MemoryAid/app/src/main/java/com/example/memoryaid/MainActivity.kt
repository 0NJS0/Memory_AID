package com.memoryaid

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.memoryaid.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    private var lastTagId: String? = null
    private lateinit var memoryDb: MemoryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        memoryDb = MemoryDatabase.getDatabase(this)

        // Setup NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC not supported on this device", Toast.LENGTH_LONG).show()
            finish()
        }

        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC is disabled, opening settings...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }

        // RecyclerView setup
        binding.memoryRecyclerView.layoutManager = LinearLayoutManager(this)

        // Add Memory Button
        binding.addMemoryButton.setOnClickListener {
            if (lastTagId == null) {
                Toast.makeText(this, "Please scan an NFC tag first", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, AddMemoryActivity::class.java)
                intent.putExtra("tagId", lastTagId)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)

        // Refresh memory list on resume
        lastTagId?.let { tagId ->
            lifecycleScope.launch {
                val memories = memoryDb.memoryDao().getMemoriesByTag(tagId)
                runOnUiThread {
                    binding.memoryRecyclerView.adapter = MemoryAdapter(
                        memories.map {
                            Memory(
                                id = it.id.toLong(),
                                tagId = it.tagId,
                                title = it.title,
                                description = it.description,
                                mediaUri = it.mediaUri
                            )
                        }
                    ) { memory ->
                        val editIntent = Intent(this@MainActivity, EditMemoryActivity::class.java)
                        editIntent.putExtra("memoryId", memory.id.toInt())
                        startActivity(editIntent)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            val tagId = tag?.id?.joinToString("") { "%02x".format(it) } ?: return

            lastTagId = tagId
            binding.nfcStatus.text = "Scanned Tag: $tagId"

            if (!this::memoryDb.isInitialized || isFinishing || isDestroyed) return

            lifecycleScope.launch {
                try {
                    val memories = memoryDb.memoryDao().getMemoriesByTag(tagId)
                    runOnUiThread {
                        binding.memoryRecyclerView.adapter = MemoryAdapter(
                            memories.map {
                                Memory(
                                    id = it.id.toLong(),
                                    tagId = it.tagId,
                                    title = it.title,
                                    description = it.description,
                                    mediaUri = it.mediaUri
                                )
                            }
                        ) { memory ->
                            val editIntent = Intent(this@MainActivity, EditMemoryActivity::class.java)
                            editIntent.putExtra("memoryId", memory.id.toInt())
                            startActivity(editIntent)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error loading memories", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
