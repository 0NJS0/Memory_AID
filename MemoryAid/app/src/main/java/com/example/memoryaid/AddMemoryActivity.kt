package com.memoryaid

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.memoryaid.databinding.ActivityAddMemoryBinding
import kotlinx.coroutines.launch

class AddMemoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMemoryBinding
    private lateinit var db: MemoryDatabase
    private lateinit var tagId: String
    private var selectedMediaUri: Uri? = null

    companion object {
        private const val PICK_MEDIA_REQUEST = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tagId = intent.getStringExtra("tagId") ?: return
        db = MemoryDatabase.getDatabase(this)

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

            lifecycleScope.launch {
                db.memoryDao().insert(
                    MemoryEntity(
                        tagId = tagId,
                        title = title,
                        description = desc,
                        mediaUri = selectedMediaUri?.toString()
                    )
                )
                Toast.makeText(this@AddMemoryActivity, "Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
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
