package com.example.app_music

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityTopHits : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_hits)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTopSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 1. Kết nối tới Firebase
        val database = FirebaseDatabase.getInstance().getReference("Songs")

        // 2. Tải danh sách và sắp xếp theo lượt thích (likes)
        database.orderByChild("likes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val topSongs = ArrayList<Song>()
                for (songSnapshot in snapshot.children) {
                    val song = songSnapshot.getValue(Song::class.java)
                    // Chỉ lấy những bài có từ 2 lượt thích trở lên (để đưa lên Top)
                    if (song != null && song.likes >= 2) {
                        topSongs.add(song)
                    }
                }

                // Mặc định Firebase sắp xếp tăng dần -> Ta phải đảo ngược mảng để lượt like cao nhất lên đầu
                topSongs.reverse()

                // Chỉ lấy tối đa 5 bài hát đầu tiên
                val finalTopSongs = ArrayList(topSongs.take(5))

                val adapter = TopSongAdapter(finalTopSongs)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ActivityTopHits, "Lỗi mạng!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}