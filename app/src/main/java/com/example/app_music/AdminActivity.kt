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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminActivity : AppCompatActivity() {

    private lateinit var rvAdminSongs: RecyclerView
    private lateinit var adapter: AdminSongAdapter
    private var songList = ArrayList<Song>()
    private var fullSongList = ArrayList<Song>() // Giữ danh sách gốc để search
    private lateinit var database: DatabaseReference // Khai báo Firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Khởi tạo kết nối đến node "Songs" trong Realtime Database
        database = FirebaseDatabase.getInstance().getReference("Songs")

        rvAdminSongs = findViewById(R.id.rvAdminSongs)
        rvAdminSongs.layoutManager = LinearLayoutManager(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val fabAddSong = findViewById<FloatingActionButton>(R.id.fabAddSong)
        val edtAdminSearch = findViewById<EditText>(R.id.edtAdminSearch)

        btnBack.setOnClickListener { finish() }

        fabAddSong.setOnClickListener {
            showSongDialog(null)
        }

        edtAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSongs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupRecyclerView()
        fetchSongsFromFirebase() // Tự động lấy dữ liệu từ Firebase
    }

    private fun setupRecyclerView() {
        adapter = AdminSongAdapter(songList,
            onEditClick = { song -> showSongDialog(song) },
            onDeleteClick = { song -> confirmDeleteSong(song) }
        )
        rvAdminSongs.adapter = adapter
    }

    private fun fetchSongsFromFirebase() {
        // Lắng nghe dữ liệu thay đổi trên Firebase (tự động cập nhật realtime)
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
                adapter.updateData(songList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminActivity, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterSongs(text: String) {
        val filteredList = ArrayList<Song>()
        for (song in fullSongList) {
            if (song.title.lowercase().contains(text.lowercase()) ||
                song.artist.lowercase().contains(text.lowercase())
            ) {
                filteredList.add(song)
            }
        }
        adapter.updateData(filteredList)
    }

    private fun showSongDialog(song: Song?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_song_form, null)

        val edtTitle = dialogView.findViewById<EditText>(R.id.edtDialogTitle)
        val edtArtist = dialogView.findViewById<EditText>(R.id.edtDialogArtist)
        val edtPath = dialogView.findViewById<EditText>(R.id.edtDialogPath)
        val edtImage = dialogView.findViewById<EditText>(R.id.edtDialogImage)
        val edtLyrics = dialogView.findViewById<EditText>(R.id.edtDialogLyrics)

        // Đổi hint để Admin biết chỗ dán link
        edtPath.hint = "Dán link bài hát (VD: link đuôi .mp3)"
        edtImage.hint = "Dán link ảnh bìa (VD: link Google, Imgur...)"

        val isEditMode = (song != null)

        if (isEditMode) {
            edtTitle.setText(song!!.title)
            edtArtist.setText(song.artist)
            edtPath.setText(song.path)
            edtImage.setText(song.image)
            edtLyrics.setText(song.lyrics)
        }

        val dialogTitle = if (isEditMode) "Sửa Bài Hát" else "Thêm Bài Hát Mới"

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
                    Toast.makeText(this, "Tên, Ca sĩ và Link nhạc không được để trống!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (isEditMode) {
                    // Cập nhật lên Firebase
                    val updateData = mapOf(
                        "title" to title,
                        "artist" to artist,
                        "path" to path,
                        "image" to image,
                        "lyrics" to lyrics
                    )
                    database.child(song!!.id).updateChildren(updateData).addOnCompleteListener { task ->
                        if (task.isSuccessful) Toast.makeText(this, "Đã cập nhật bài hát!", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Thêm mới lên Firebase
                    val newId = database.push().key ?: return@setPositiveButton
                    val newSong = Song(newId, title, artist, path, image, lyrics, 0)

                    database.child(newId).setValue(newSong).addOnCompleteListener { task ->
                        if (task.isSuccessful) Toast.makeText(this, "Đã thêm lên mây!", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this, "Lỗi khi thêm!", Toast.LENGTH_SHORT).show()
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
            .setMessage("Bạn có chắc chắn muốn xóa bài '${song.title}' khỏi hệ thống không?")
            .setPositiveButton("Xóa") { _, _ ->
                database.child(song.id).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) Toast.makeText(this, "Đã xóa bài hát!", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Lỗi khi xóa!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}