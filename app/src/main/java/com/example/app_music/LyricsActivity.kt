package com.example.app_music

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LyricsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lyrics)

        val tvFullLyrics = findViewById<TextView>(R.id.tvFullLyrics)
        val btnCloseLyrics = findViewById<Button>(R.id.btnCloseLyrics)

        // Nhận lời bài hát từ Player truyền qua (nếu không có thì báo lỗi)
        val lyrics = intent.getStringExtra("SONG_LYRICS") ?: "Đang cập nhật lời bài hát..."
        tvFullLyrics.text = lyrics

        // Bấm nút Đóng -> Tắt trang này, tự động quay về Player
        btnCloseLyrics.setOnClickListener {
            finish()
        }
    }
}