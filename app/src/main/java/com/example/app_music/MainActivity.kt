package com.example.app_music

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var songList = ArrayList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ánh xạ View và khởi tạo DBHelper
        dbHelper = DBHelper(this)
        recyclerView = findViewById(R.id.recyclerViewSongs)
        val edtSearch = findViewById<EditText>(R.id.edtSearch)

        val navHome = findViewById<ImageView>(R.id.navHome)
        val navDownload = findViewById<ImageView>(R.id.navDownload) // Ánh xạ nút Download mới
        val navAdminTop = findViewById<ImageView>(R.id.navAdminTop) // Ánh xạ nút Admin ở góc trên
        val navPlay = findViewById<ImageView>(R.id.navPlay)
        val navLogout = findViewById<ImageView>(R.id.navLogout)
        val tvSeeAll = findViewById<TextView>(R.id.tvSeeAll)

        edtSearch.background.alpha = 255

        // 2. Chèn dữ liệu mẫu nếu DB trống
        if (dbHelper.getAllSongs().isEmpty()) {
            dbHelper.addSong("Đom Đóm", "Jack - J97", "nhac_1", "nen", "Em đi mất rồi, còn anh ở lại...")
            dbHelper.addSong("Hồng Nhan", "Jack x K-ICM", "nhac_2", "nen1", "Nhân duyên đứt đoạn, để lại lỡ làng...")
            dbHelper.addSong("Sóng Gió", "Jack x K-ICM", "nhac_3", "nen2", "Hồng trần trên đôi cánh tay...")
        }

        // 3. Thiết lập RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // 4. LOGIC THANH TÌM KIẾM
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSongs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // CÀI ĐẶT HIỆU ỨNG NHÚN KHI CHẠM CHO CÁC NÚT
        setupButtonAnimations(navHome)
        setupButtonAnimations(navDownload)
        setupButtonAnimations(navAdminTop)
        setupButtonAnimations(navPlay)
        setupButtonAnimations(navLogout)

        // SỰ KIỆN CLICK "XEM TẤT CẢ"
        tvSeeAll.setOnClickListener {
            val intent = Intent(this, ActivityTopHits::class.java)
            startActivity(intent)
        }

        // 5. SỰ KIỆN CLICK MENU
        navHome.setOnClickListener {
            Toast.makeText(this, "Đang ở Trang chủ", Toast.LENGTH_SHORT).show()
        }

        // Sự kiện click cho nút Admin (Góc trên phải)
        navAdminTop.setOnClickListener {
            val sharedPref = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE)
            val role = sharedPref.getInt("role", -1)

            if (role == 1) {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                Toast.makeText(this, "Chỉ Quản trị viên mới được dùng chức năng này!", Toast.LENGTH_LONG).show()
            }
        }

        // Sự kiện click cho nút Download (Thanh menu dưới)
        navDownload.setOnClickListener {
            startActivity(Intent(this, DownloadActivity::class.java))
        }

        navPlay.setOnClickListener {
            Toast.makeText(this, "Tính năng phát ngẫu nhiên", Toast.LENGTH_SHORT).show()
        }

        navLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("isLoggedIn", false)
                putInt("role", -1)
                apply()
            }
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // HÀM TẠO HIỆU ỨNG CO GIÃN (CLICK ANIMATION)
    private fun setupButtonAnimations(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                }
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        refreshMusicList()
    }

    private fun refreshMusicList() {
        songList = dbHelper.getAllSongs()
        songAdapter = SongAdapter(songList)
        recyclerView.adapter = songAdapter
    }

    private fun filterSongs(text: String) {
        val filteredList = ArrayList<Song>()
        for (song in songList) {
            if (song.title.lowercase().contains(text.lowercase()) ||
                song.artist.lowercase().contains(text.lowercase())) {
                filteredList.add(song)
            }
        }
        songAdapter.updateList(filteredList)
    }
}