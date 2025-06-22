package com.memoryaid

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.memoryaid.databinding.ActivityAddMemoryBinding
import kotlinx.coroutines.launch

class EditMemoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMemoryBinding
    private lateinit var db: MemoryDatabase
    private var memoryId: Int = -1
    private var memoryEntity: MemoryEntity? = null
    private var selectedMediaUri: Uri? = null

    companion object {
        private const val PICK_MEDIA_REQUEST = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = MemoryDatabase.getDatabase(this)
        memoryId = intent.getIntExtra("memoryId", -1)

        if (memoryId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            memoryEntity = db.memoryDao().getMemoryById(memoryId)
            memoryEntity?.let { memory ->
                selectedMediaUri = memory.mediaUri?.toUri()
                runOnUiThread {
                    binding.inputTitle.setText(memory.title)
                    binding.inputDescription.setText(memory.description)

                    if (memory.mediaUri != null) {
                        binding.mediaPreview.setImageURI(Uri.parse(memory.mediaUri))
                        binding.mediaPreview.visibility = android.view.View.VISIBLE
                    }

                    binding.saveButton.text = "Update Memory"
                }
            }
        }

        binding.selectMediaButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            startActivityForResult(intent, PICK_MEDIA_REQUEST)
        }

        binding.saveButton.setOnClickListener {
            val title = binding.inputTitle.text.toString().trim()
            val desc = binding.inputDescription.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            memoryEntity?.let { mem ->
                val updated = mem.copy(
                    title = title,
                    description = desc,
                    mediaUri = selectedMediaUri?.toString()
                )
                lifecycleScope.launch {
                    db.memoryDao().update(updated)
                    runOnUiThread {
                        Toast.makeText(this@EditMemoryActivity, "Updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        binding.saveButton.setOnLongClickListener {
            memoryEntity?.let {
                lifecycleScope.launch {
                    db.memoryDao().delete(it)
                    runOnUiThread {
                        Toast.makeText(this@EditMemoryActivity, "Deleted!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedMediaUri = data?.data

            selectedMediaUri?.let {
                try {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

                binding.mediaPreview.setImageURI(it)
                binding.mediaPreview.visibility = android.view.View.VISIBLE
            }
        }
    }
}
