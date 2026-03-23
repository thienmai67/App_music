package com.example.app_music

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var edtSongId: EditText
    private lateinit var edtSongTitle: EditText
    private lateinit var edtArtistName: EditText
    private lateinit var edtSongPath: EditText
    private lateinit var edtSongImage: EditText // Biến cho ảnh
    private lateinit var edtLyrics: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        dbHelper = DBHelper(this)

        edtSongId = findViewById(R.id.edtSongId)
        edtSongTitle = findViewById(R.id.edtSongTitle)
        edtArtistName = findViewById(R.id.edtArtistName)
        edtSongPath = findViewById(R.id.edtSongPath)
        edtSongImage = findViewById(R.id.edtSongImage) // Ánh xạ
        edtLyrics = findViewById(R.id.edtLyrics)

        val btnAddSong = findViewById<Button>(R.id.btnAddSong)
        val btnUpdateSong = findViewById<Button>(R.id.btnUpdateSong)
        val btnDeleteSong = findViewById<Button>(R.id.btnDeleteSong)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        btnAddSong.setOnClickListener {
            val title = edtSongTitle.text.toString().trim()
            val artist = edtArtistName.text.toString().trim()
            val path = edtSongPath.text.toString().trim()
            val image = edtSongImage.text.toString().trim()
            val lyrics = edtLyrics.text.toString().trim()

            if (title.isEmpty() || artist.isEmpty() || path.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ: Tên, Ca sĩ và File nhạc!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = dbHelper.addSong(title, artist, path, image, lyrics)
            if (result != -1L) {
                Toast.makeText(this, "Thêm bài hát mới thành công!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Lỗi khi thêm bài hát!", Toast.LENGTH_SHORT).show()
            }
        }

        btnUpdateSong.setOnClickListener {
            val id = edtSongId.text.toString().trim()
            val title = edtSongTitle.text.toString().trim()
            val artist = edtArtistName.text.toString().trim()
            val path = edtSongPath.text.toString().trim()
            val image = edtSongImage.text.toString().trim()
            val lyrics = edtLyrics.text.toString().trim()

            if (id.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ID bài hát cần sửa!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.updateSong(id, title, artist, path, image, lyrics)) {
                Toast.makeText(this, "Đã cập nhật bài hát ID #$id", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Không tìm thấy ID #$id để sửa!", Toast.LENGTH_SHORT).show()
            }
        }

        btnDeleteSong.setOnClickListener {
            val id = edtSongId.text.toString().trim()
            if (id.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ID bài hát cần xóa!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dbHelper.deleteSong(id)) {
                Toast.makeText(this, "Đã xóa vĩnh viễn bài hát ID #$id", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Xóa thất bại! Không có bài hát nào mang ID #$id", Toast.LENGTH_SHORT).show()
            }
        }
    }
}