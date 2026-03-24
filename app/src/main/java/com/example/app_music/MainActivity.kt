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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var songList = ArrayList<Song>()
    private var fullSongList = ArrayList<Song>()

    // Khai báo Firebase thay cho DBHelper
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val targetMode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (currentMode != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }

        setContentView(R.layout.activity_main)

        // Kết nối đến kho dữ liệu bài hát trên Firebase
        database = FirebaseDatabase.getInstance().getReference("Songs")

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

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        songAdapter = SongAdapter(songList)
        recyclerView.adapter = songAdapter

        // Tự động tải danh sách nhạc từ Firebase
        fetchSongsFromFirebase()

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSongs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupButtonAnimations(navHome)
        setupButtonAnimations(navDownload)
        setupButtonAnimations(navAdminTop)
        setupButtonAnimations(navSettingsTop)
        setupButtonAnimations(navPlay)
        setupButtonAnimations(navLogout)

        tvSeeAll.setOnClickListener {
            startActivity(Intent(this, ActivityTopHits::class.java))
        }

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

        navSettingsTop.setOnClickListener {
            val switchView = androidx.appcompat.widget.SwitchCompat(this)
            switchView.text = " Giao diện tối (Dark Mode)"
            switchView.textSize = 16f
            switchView.isChecked = sharedPref.getBoolean("isDarkMode", false)
            switchView.setPadding(60, 60, 60, 60)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cài đặt")
                .setView(switchView)
                .setPositiveButton("Đóng", null)
                .create()

            dialog.show()

            switchView.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    sharedPref.edit().putBoolean("isDarkMode", isChecked).apply()
                    dialog.dismiss()

                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        AppCompatDelegate.setDefaultNightMode(newMode)
                    }, 200)
                }
            }
        }
    }

    private fun fetchSongsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                songList.clear()
                fullSongList.clear()
                for (songSnapshot in snapshot.children) {
                    val song = songSnapshot.getValue(Song::class.java)
                    if (song != null) {
                        songList.add(song)
                        fullSongList.add(song)
                    }
                }
                songAdapter.updateList(songList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Lỗi tải dữ liệu mạng", Toast.LENGTH_SHORT).show()
            }
        })
    }

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

    private fun filterSongs(text: String) {
        if (!::songAdapter.isInitialized) return

        val filteredList = ArrayList<Song>()
        for (song in fullSongList) {
            if (song.title.lowercase().contains(text.lowercase()) ||
                song.artist.lowercase().contains(text.lowercase())) {
                filteredList.add(song)
            }
        }
        songAdapter.updateList(filteredList)
    }
}