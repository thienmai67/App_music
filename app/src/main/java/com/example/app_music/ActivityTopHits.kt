package com.example.app_music

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActivityTopHits : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Gọi file giao diện XML mà mình đã gửi cho bạn ở mấy tin nhắn trước
        setContentView(R.layout.activity_top_hits)

        // Xử lý nút Back (quay lại trang chủ)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Đóng trang hiện tại để quay về trang trước đó
        }

        // Thiết lập RecyclerView để hiển thị danh sách
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTopSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Lấy danh sách Top bài hát từ Database
        val dbHelper = DBHelper(this)
        val topSongs = dbHelper.getTopLikedSongs()

        // Gắn Adapter vào RecyclerView
        val adapter = TopSongAdapter(topSongs)
        recyclerView.adapter = adapter
    }
}