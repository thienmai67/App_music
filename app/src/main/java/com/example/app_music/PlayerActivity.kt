package com.example.app_music

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.concurrent.thread

class PlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var tvLyrics: TextView
    private lateinit var tvSongTitle: TextView
    private lateinit var tvArtistName: TextView
    private lateinit var imgAlbumArt: ImageView
    private lateinit var btnLike: LinearLayout
    private lateinit var imgLike: ImageView
    private lateinit var tvLikeCount: TextView
    private lateinit var btnShare: ImageView
    private lateinit var btnDownload: ImageView

    private var isLoggedIn = false
    private var isLiked = false
    private val handler = Handler(Looper.getMainLooper())

    private var songId = "" // ĐÃ SỬA: Đổi thành String để tương thích mây Firebase
    private var songTitle = ""
    private var songArtist = ""
    private var songPath = ""
    private var songLyrics = ""
    private var songImage = ""
    private var likeCount = 0
    private var songLocalPath: String? = null

    private var albumAnimator: ObjectAnimator? = null
    private var isPrepared = false // Cờ kiểm tra nhạc mây đã tải xong chưa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val sharedPref = getSharedPreferences("AppMusicPrefs", MODE_PRIVATE)
        isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        initViews()
        loadSongData()
        setupActions()
    }

    private fun initViews() {
        seekBar = findViewById(R.id.seekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        tvLyrics = findViewById(R.id.tvLyrics)
        tvSongTitle = findViewById(R.id.tvSongTitle)
        tvArtistName = findViewById(R.id.tvArtistName)
        imgAlbumArt = findViewById(R.id.imgAlbumArt)
        btnLike = findViewById(R.id.btnLike)
        imgLike = findViewById(R.id.imgLike)
        tvLikeCount = findViewById(R.id.tvLikeCount)
        btnShare = findViewById(R.id.btnShare)
        btnDownload = findViewById(R.id.btnDownload)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        tvLyrics.setOnClickListener {
            if (isLoggedIn) {
                if (songLyrics.trim().isEmpty() || songLyrics == "Chưa có lời bài hát") {
                    Toast.makeText(this, "Bài hát này hiện chưa có lời nhạc!", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, LyricsActivity::class.java)
                    intent.putExtra("SONG_LYRICS", songLyrics)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Bạn cần đăng nhập để xem lời!", Toast.LENGTH_SHORT).show()
            }
        }

        albumAnimator = ObjectAnimator.ofFloat(imgAlbumArt, "rotation", 0f, 360f)
        albumAnimator?.duration = 10000
        albumAnimator?.repeatCount = ObjectAnimator.INFINITE
        albumAnimator?.interpolator = LinearInterpolator()
    }

    private fun loadSongData() {
        songId = intent.getStringExtra("song_id") ?: ""
        songTitle = intent.getStringExtra("SONG_TITLE") ?: "Tên bài hát"
        songArtist = intent.getStringExtra("SONG_ARTIST") ?: "Ca sĩ"
        songPath = intent.getStringExtra("SONG_PATH") ?: "test_song"

        songLocalPath = intent.getStringExtra("SONG_LOCAL_PATH")
        val songLocalImage = intent.getStringExtra("SONG_LOCAL_IMAGE")

        songImage = intent.getStringExtra("SONG_IMAGE") ?: ""
        songLyrics = intent.getStringExtra("SONG_LYRICS") ?: "Chưa có lời bài hát"
        likeCount = intent.getIntExtra("song_likes", 0)

        tvSongTitle.text = songTitle
        tvArtistName.text = songArtist
        updateLikeUI()

        // TẢI ẢNH: Ưu tiên ảnh Local -> Link Mạng (Glide) -> Ảnh Raw cũ
        if (!songLocalImage.isNullOrEmpty() && File(songLocalImage).exists()) {
            val bitmap = BitmapFactory.decodeFile(songLocalImage)
            imgAlbumArt.setImageBitmap(bitmap)
        } else if (songImage.startsWith("http")) {
            Glide.with(this).load(songImage).into(imgAlbumArt)
        } else {
            val imageResId = resources.getIdentifier(songImage, "drawable", packageName)
            if (imageResId != 0) imgAlbumArt.setImageResource(imageResId)
            else imgAlbumArt.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        setupPlayer()
    }

    private fun setupActions() {
        btnLike.setOnClickListener {
            if (songId.isEmpty()) {
                Toast.makeText(this, "Lỗi không xác định bài hát!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isLiked = !isLiked
            if (isLiked) {
                likeCount++
                Toast.makeText(this, "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show()
            } else {
                likeCount--
                Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show()
            }

            // ĐÃ CẬP NHẬT: Ghi lượt thích thẳng lên Firebase Realtime Database
            FirebaseDatabase.getInstance().getReference("Songs").child(songId).child("likes").setValue(likeCount)
            updateLikeUI()
        }

        btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Đang nghe bài '$songTitle' của $songArtist trên App Music Nhóm 4. Nghe cùng mình nhé!")
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài hát qua..."))
        }

        btnDownload.setOnClickListener {
            if (!isLoggedIn) {
                Toast.makeText(this, "Vui lòng đăng nhập để tải nhạc!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val musicDir = File(downloadsDir, "AppMusic")
            val baseFileName = "$songTitle - $songArtist"
            val mp3File = File(musicDir, "$baseFileName.mp3")

            if (!songLocalPath.isNullOrEmpty() || mp3File.exists()) {
                Toast.makeText(this, "Bài hát này đã có sẵn trong máy!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Đang tải bài hát xuống...", Toast.LENGTH_SHORT).show()

            // Dùng luồng chạy ngầm để tải file từ Link mạng về máy
            thread {
                try {
                    if (!musicDir.exists()) musicDir.mkdirs()

                    if (songPath.startsWith("http")) {
                        URL(songPath).openStream().use { input ->
                            FileOutputStream(mp3File).use { output -> input.copyTo(output) }
                        }
                    } else {
                        val resID = resources.getIdentifier(songPath, "raw", packageName)
                        if (resID != 0) {
                            resources.openRawResource(resID).use { input ->
                                FileOutputStream(mp3File).use { output -> input.copyTo(output) }
                            }
                        }
                    }

                    // Tải kèm ảnh bìa nếu là link mạng
                    if (songImage.startsWith("http")) {
                        val imgFile = File(musicDir, "$baseFileName.jpg")
                        URL(songImage).openStream().use { input ->
                            FileOutputStream(imgFile).use { output -> input.copyTo(output) }
                        }
                    }

                    runOnUiThread {
                        Toast.makeText(this@PlayerActivity, "Tải xong: $songTitle", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread { Toast.makeText(this@PlayerActivity, "Lỗi tải xuống: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    private fun setupPlayer() {
        try {
            mediaPlayer = MediaPlayer()

            if (!songLocalPath.isNullOrEmpty() && File(songLocalPath!!).exists()) {
                // Nhạc tải về trong máy
                mediaPlayer.setDataSource(songLocalPath)
                mediaPlayer.prepare()
                onPrepared()
            } else if (songPath.startsWith("http")) {
                // Nhạc trên mây (Streaming qua URL)
                mediaPlayer.setDataSource(songPath)
                mediaPlayer.prepareAsync() // Chạy ngầm để không đơ app khi mạng chậm
                mediaPlayer.setOnPreparedListener {
                    onPrepared()
                }
            } else {
                // Nhạc offline mặc định có sẵn (R.raw)
                val resID = resources.getIdentifier(songPath, "raw", packageName)
                if (resID != 0) {
                    val afd = resources.openRawResourceFd(resID)
                    mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    mediaPlayer.prepare()
                    onPrepared()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khi tải file nhạc!", Toast.LENGTH_SHORT).show()
        }

        btnPlayPause.setOnClickListener {
            if (!isPrepared) {
                Toast.makeText(this, "Đang tải nhạc, vui lòng đợi...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                albumAnimator?.pause()
            } else {
                mediaPlayer.start()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                updateSeekBar()
                if (albumAnimator?.isStarted == true) albumAnimator?.resume()
                else albumAnimator?.start()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = createTimeLabel(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { handler.removeCallbacksAndMessages(null) }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isPrepared) {
                    mediaPlayer.seekTo(seekBar!!.progress)
                    updateSeekBar()
                }
            }
        })
    }

    private fun onPrepared() {
        isPrepared = true
        seekBar.max = mediaPlayer.duration
        tvTotalTime.text = createTimeLabel(mediaPlayer.duration)
        // Tự động phát khi tải xong
        mediaPlayer.start()
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        updateSeekBar()
        albumAnimator?.start()
    }

    private fun updateSeekBar() {
        if (isPrepared && mediaPlayer.isPlaying) {
            val currentPos = mediaPlayer.currentPosition

            // Khách bị giới hạn 30 giây
            if (!isLoggedIn && currentPos >= 30000 && songLocalPath.isNullOrEmpty()) {
                mediaPlayer.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                albumAnimator?.pause()
                seekBar.progress = 30000
                Toast.makeText(this, "Hết thời gian nghe thử! Hãy đăng nhập.", Toast.LENGTH_LONG).show()
                return
            }
            seekBar.progress = currentPos
            tvCurrentTime.text = createTimeLabel(currentPos)
            handler.postDelayed({ updateSeekBar() }, 1000)
        }
    }

    private fun updateLikeUI() {
        tvLikeCount.text = formatLikes(likeCount)
        imgLike.setImageResource(if (isLiked) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
    }

    private fun formatLikes(likes: Int): String {
        return if (likes >= 1000) String.format("%.1fk", likes / 1000.0) else likes.toString()
    }

    private fun createTimeLabel(time: Int): String {
        val min = time / 1000 / 60
        val sec = time / 1000 % 60
        return String.format("%02d:%02d", min, sec)
    }

    override fun onPause() {
        super.onPause()
        if (isPrepared && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            albumAnimator?.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPrepared) {
            mediaPlayer.release()
            handler.removeCallbacksAndMessages(null)
            albumAnimator?.cancel()
        }
    }
}