package com.example.videoframes


import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        val frames = mutableListOf<Bitmap>()
        val adapter = FrameAdapter(this, frames)
        recyclerView.adapter = adapter

        val buttonExtract: Button = findViewById(R.id.button_extract)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE)
        }

        buttonExtract.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                extractAllFrames(frames, adapter)
            } else {
                Toast.makeText(this, "Permission is required to access the video.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun extractAllFrames(frames: MutableList<Bitmap>, adapter: FrameAdapter) {

        val retriever = MediaMetadataRetriever()

        try {
            Toast.makeText(this, "Init to extract frames to video.", Toast.LENGTH_LONG).show()
            retriever.setDataSource("/sdcard/Download/videoteste.mp4")
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            val duration = durationString?.toLongOrNull() ?: 0L
            val interval = 1000000L

            for (time in 0 until duration * 1000 step interval) {
                val frame: Bitmap? = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST)
                frame?.let {
                    frames.add(it)
                    runOnUiThread {
                        adapter.notifyItemInserted(frames.size - 1)
                    }
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Failed to extract frames: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            retriever.release()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_LONG).show()
            }
        }
    }
}