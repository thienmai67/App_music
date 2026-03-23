package com.example.app_music

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var rvAdminSongs: RecyclerView
    private lateinit var adapter: AdminSongAdapter
    private var songList = ArrayList<Song>() // Danh sách gốc chứa toàn bộ bài hát

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        dbHelper = DBHelper(this)

        rvAdminSongs = findViewById(R.id.rvAdminSongs)
        rvAdminSongs.layoutManager = LinearLayoutManager(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val fabAddSong = findViewById<FloatingActionButton>(R.id.fabAddSong)
        val edtAdminSearch = findViewById<EditText>(R.id.edtAdminSearch) // Ánh xạ thanh tìm kiếm

        btnBack.setOnClickListener { finish() }

        // Mở dialog trắng để Thêm mới
        fabAddSong.setOnClickListener {
            showSongDialog(null)
        }

        // Bắt sự kiện khi gõ vào thanh tìm kiếm
        edtAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSongs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        songList = dbHelper.getAllSongs()
        adapter = AdminSongAdapter(songList,
            onEditClick = { song ->
                // Mở dialog và truyền dữ liệu bài hát cũ vào để Sửa
                showSongDialog(song)
            },
            onDeleteClick = { song ->
                confirmDeleteSong(song)
            }
        )
        rvAdminSongs.adapter = adapter
    }

    private fun refreshList() {
        songList = dbHelper.getAllSongs()
        // Clear ô search khi có thay đổi dữ liệu (tùy chọn)
        findViewById<EditText>(R.id.edtAdminSearch).text.clear()
        adapter.updateData(songList)
    }

    // Hàm lọc danh sách bài hát
    private fun filterSongs(text: String) {
        val filteredList = ArrayList<Song>()
        for (song in songList) {
            // Tìm kiếm không phân biệt hoa thường theo Tên bài hát hoặc Tên ca sĩ
            if (song.title.lowercase().contains(text.lowercase()) ||
                song.artist.lowercase().contains(text.lowercase())
            ) {
                filteredList.add(song)
            }
        }
        adapter.updateData(filteredList)
    }

    // Hàm hiển thị Dialog dùng chung cho cả Thêm và Sửa
    private fun showSongDialog(song: Song?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_song_form, null)

        val edtTitle = dialogView.findViewById<EditText>(R.id.edtDialogTitle)
        val edtArtist = dialogView.findViewById<EditText>(R.id.edtDialogArtist)
        val edtPath = dialogView.findViewById<EditText>(R.id.edtDialogPath)
        val edtImage = dialogView.findViewById<EditText>(R.id.edtDialogImage)
        val edtLyrics = dialogView.findViewById<EditText>(R.id.edtDialogLyrics)

        val isEditMode = (song != null)

        // Nếu là chế độ sửa, điền sẵn dữ liệu cũ vào các ô
        if (isEditMode) {
            edtTitle.setText(song!!.title)
            edtArtist.setText(song.artist)
            edtPath.setText(song.path)
            edtImage.setText(song.image)
            edtLyrics.setText(song.lyrics)
        }

        val dialogTitle = if (isEditMode) "Sửa Bài Hát #${song?.id}" else "Thêm Bài Hát Mới"

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val title = edtTitle.text.toString().trim()
                val artist = edtArtist.text.toString().trim()
                val path = edtPath.text.toString().trim()
                val image = edtImage.text.toString().trim()
                val lyrics = edtLyrics.text.toString().trim()

                if (title.isEmpty() || artist.isEmpty() || path.isEmpty()) {
                    Toast.makeText(this, "Tên, Ca sĩ và File nhạc không được để trống!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (isEditMode) {
                    // Cập nhật
                    val success = dbHelper.updateSong(song!!.id.toString(), title, artist, path, image, lyrics)
                    if (success) {
                        Toast.makeText(this, "Đã cập nhật bài hát!", Toast.LENGTH_SHORT).show()
                        refreshList()
                    } else {
                        Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Thêm mới
                    val result = dbHelper.addSong(title, artist, path, image, lyrics)
                    if (result != -1L) {
                        Toast.makeText(this, "Đã thêm bài hát mới!", Toast.LENGTH_SHORT).show()
                        refreshList()
                    } else {
                        Toast.makeText(this, "Lỗi khi thêm bài hát!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .create()
            .show()
    }

    private fun confirmDeleteSong(song: Song) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa bài hát '${song.title}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                if (dbHelper.deleteSong(song.id.toString())) {
                    Toast.makeText(this, "Đã xóa bài hát", Toast.LENGTH_SHORT).show()
                    refreshList()
                } else {
                    Toast.makeText(this, "Lỗi khi xóa!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}