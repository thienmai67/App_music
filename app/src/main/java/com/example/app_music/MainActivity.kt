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
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var songList = ArrayList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. KIỂM TRA MÀU NỀN TRƯỚC KHI KHỞI TẠO (Chống vòng lặp và chớp đen)
        val sharedPref = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val targetMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (currentMode != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }

        setContentView(R.layout.activity_main)

        // 2. Ánh xạ View và khởi tạo DBHelper
        dbHelper = DBHelper(this)
        recyclerView = findViewById(R.id.recyclerViewSongs)
        val edtSearch = findViewById<EditText>(R.id.edtSearch)

        val navHome = findViewById<ImageView>(R.id.navHome)
        val navDownload = findViewById<ImageView>(R.id.navDownload)
        val navAdminTop = findViewById<ImageView>(R.id.navAdminTop)
        val navSettingsTop = findViewById<ImageView>(R.id.navSettingsTop)
        val navPlay = findViewById<ImageView>(R.id.navPlay)
        val navLogout = findViewById<ImageView>(R.id.navLogout)
        val tvSeeAll = findViewById<TextView>(R.id.tvSeeAll)

        edtSearch.background.alpha = 255

        // 3. Chèn dữ liệu mẫu nếu DB trống
        if (dbHelper.getAllSongs().isEmpty()) {
            dbHelper.addSong("Đom Đóm", "Jack - J97", "nhac_1", "nen", "Em đi mất rồi, còn anh ở lại...")
            dbHelper.addSong("Hồng Nhan", "Jack x K-ICM", "nhac_2", "nen1", "Nhân duyên đứt đoạn, để lại lỡ làng...")
            dbHelper.addSong("Sóng Gió", "Jack x K-ICM", "nhac_3", "nen2", "Hồng trần trên đôi cánh tay...")
        }

        // 4. THIẾT LẬP RECYCLERVIEW VÀ KHỞI TẠO ADAPTER NGAY TẠI ĐÂY (TRỊ LỖI VĂNG APP)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        songList = dbHelper.getAllSongs()
        songAdapter = SongAdapter(songList)
        recyclerView.adapter = songAdapter

        // 5. LOGIC THANH TÌM KIẾM
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
        setupButtonAnimations(navSettingsTop)
        setupButtonAnimations(navPlay)
        setupButtonAnimations(navLogout)

        // SỰ KIỆN CLICK "XEM TẤT CẢ"
        tvSeeAll.setOnClickListener {
            val intent = Intent(this, ActivityTopHits::class.java)
            startActivity(intent)
        }

        // SỰ KIỆN CLICK MENU
        navHome.setOnClickListener {
            Toast.makeText(this, "Đang ở Trang chủ", Toast.LENGTH_SHORT).show()
        }

        navAdminTop.setOnClickListener {
            val role = sharedPref.getInt("role", -1)
            if (role == 1) {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                Toast.makeText(this, "Chỉ Quản trị viên mới được dùng chức năng này!", Toast.LENGTH_LONG).show()
            }
        }

        navDownload.setOnClickListener {
            startActivity(Intent(this, DownloadActivity::class.java))
        }

        navPlay.setOnClickListener {
            if (songList.isNotEmpty()) {
                val randomSong = songList.random()
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("SONG_TITLE", randomSong.title)
                intent.putExtra("SONG_ARTIST", randomSong.artist)
                intent.putExtra("SONG_PATH", randomSong.path)
                intent.putExtra("SONG_IMAGE", randomSong.image)
                intent.putExtra("SONG_LYRICS", randomSong.lyrics)
                intent.putExtra("song_id", randomSong.id)
                intent.putExtra("song_likes", randomSong.likes)

                startActivity(intent)
                Toast.makeText(this, "Đang phát ngẫu nhiên: ${randomSong.title}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Danh sách nhạc đang trống!", Toast.LENGTH_SHORT).show()
            }
        }

        navLogout.setOnClickListener {
            with(sharedPref.edit()) {
                putBoolean("isLoggedIn", false)
                putInt("role", -1)
                apply()
            }
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // --- SỰ KIỆN CLICK NÚT CÀI ĐẶT ---
        navSettingsTop.setOnClickListener {
            val switchView = androidx.appcompat.widget.SwitchCompat(this)
            switchView.text = " Giao diện tối (Dark Mode)"
            switchView.textSize = 16f

            // Ngắt sự kiện ảo lúc khởi tạo
            switchView.setOnCheckedChangeListener(null)
            switchView.isChecked = sharedPref.getBoolean("isDarkMode", false)
            switchView.setPadding(60, 60, 60, 60)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cài đặt")
                .setView(switchView)
                .setPositiveButton("Đóng", null)
                .create()

            dialog.show()

            switchView.setOnCheckedChangeListener { buttonView, isChecked ->
                // Chỉ chạy khi ngón tay người dùng thực sự chạm vào nút gạt
                if (buttonView.isPressed) {
                    sharedPref.edit().putBoolean("isDarkMode", isChecked).apply()
                    dialog.dismiss() // Đóng thông báo trước khi đổi màu

                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        AppCompatDelegate.setDefaultNightMode(newMode)
                    }, 200) // Nghỉ 0.2 giây đợi tắt hẳn Dialog
                }
            }
        }
    }

    // HÀM TẠO HIỆU ỨNG CO GIÃN
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
        // Cập nhật lại list nhạc khi quay về từ trang khác
        songList = dbHelper.getAllSongs()
        if (::songAdapter.isInitialized) {
            songAdapter.updateList(songList)
        }
    }

    private fun filterSongs(text: String) {
        if (!::songAdapter.isInitialized) return // Chốt chặn an toàn tuyệt đối cuối cùng

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