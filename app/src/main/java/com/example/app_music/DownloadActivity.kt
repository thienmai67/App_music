package com.example.app_music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DownloadActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private var downloadedFiles = ArrayList<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        recyclerView = findViewById(R.id.recyclerViewDownloads)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        checkPermissionsAndLoadFiles()
    }

    private fun checkPermissionsAndLoadFiles() {
        // Tùy thuộc vào phiên bản Android để xin quyền phù hợp
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        } else {
            loadFiles()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadFiles()
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền bộ nhớ để xem danh sách tải về!", Toast.LENGTH_SHORT).show()
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Chưa cấp quyền truy cập bộ nhớ"
        }
    }

    private fun loadFiles() {
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsFolder.exists() && downloadsFolder.isDirectory) {
            // Lọc các file .mp3
            val files = downloadsFolder.listFiles { file ->
                file.isFile && file.name.endsWith(".mp3")
            }
            if (files != null) {
                downloadedFiles.clear()
                downloadedFiles.addAll(files)
            }
        }

        if (downloadedFiles.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = DownloadAdapter(downloadedFiles)
        }
    }
}