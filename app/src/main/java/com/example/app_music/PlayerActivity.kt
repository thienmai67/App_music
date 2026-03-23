package com.example.app_music // Hãy đổi thành package name của bạn

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class PlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var tvLyrics: TextView
    private lateinit var tvSongTitle: TextView
    private lateinit var tvArtistName: TextView
    private lateinit var imgAlbumArt: ImageView // Đã ánh xạ biến cho ảnh đại diện
    private lateinit var btnLike: LinearLayout
    private lateinit var imgLike: ImageView
    private lateinit var tvLikeCount: TextView
    private lateinit var btnShare: ImageView
    private lateinit var btnDownload: ImageView
    private lateinit var dbHelper: DBHelper

    private var isLoggedIn = false
    private var isLiked = false
    private val handler = Handler(Looper.getMainLooper())

    private var songId = -1
    private var songTitle = ""
    private var songArtist = ""
    private var songPath = ""
    private var songLyrics = ""
    private var likeCount = 0

    // === PHẦN MỚI THÊM: BIẾN CHO HIỆU ỨNG XOAY ===
    private var albumAnimator: ObjectAnimator? = null
    // ============================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val sharedPref = getSharedPreferences("AppMusicPrefs", MODE_PRIVATE)
        isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        dbHelper = DBHelper(this)

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
        imgAlbumArt = findViewById(R.id.imgAlbumArt) // Ánh xạ biến
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

        // === PHẦN MỚI THÊM: KHỞI TẠO HIỆU ỨNG XOAY ===
        albumAnimator = ObjectAnimator.ofFloat(imgAlbumArt, "rotation", 0f, 360f)
        albumAnimator?.duration = 10000 // Thời gian cho 1 vòng quay (10 giây). Bạn có thể chỉnh lại.
        albumAnimator?.repeatCount = ObjectAnimator.INFINITE // Quay vô hạn
        albumAnimator?.interpolator = LinearInterpolator() // Quay đều, không giật
        // ============================================
    }

    private fun loadSongData() {
        songId = intent.getIntExtra("song_id", -1)
        songTitle = intent.getStringExtra("SONG_TITLE") ?: "Tên bài hát"
        songArtist = intent.getStringExtra("SONG_ARTIST") ?: "Ca sĩ"
        songPath = intent.getStringExtra("SONG_PATH") ?: "test_song"

        // ĐÃ SỬA: Nhận tên ảnh (kiểu String) được gửi từ danh sách bài hát
        val songImage = intent.getStringExtra("SONG_IMAGE") ?: "ic_menu_gallery"

        songLyrics = intent.getStringExtra("SONG_LYRICS") ?: "Chưa có lời bài hát"
        likeCount = intent.getIntExtra("song_likes", 0)

        tvSongTitle.text = songTitle
        tvArtistName.text = songArtist
        updateLikeUI()

        // ĐÃ SỬA: Dùng tên ảnh để tìm đúng file trong thư mục drawable và gắn vào đĩa than
        val imageResId = resources.getIdentifier(songImage, "drawable", packageName)
        if (imageResId != 0) {
            imgAlbumArt.setImageResource(imageResId)
        } else {
            imgAlbumArt.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        val resID = resources.getIdentifier(songPath, "raw", packageName)
        val finalResID = if (resID != 0) resID else R.raw.test_song
        setupPlayer(finalResID)
    }

    private fun setupActions() {
        btnLike.setOnClickListener {
            if (songId == -1) {
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
            dbHelper.updateLike(songId, likeCount)
            updateLikeUI()
        }

        btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Đang nghe bài '$songTitle' của $songArtist trên App Music xịn xò. Nghe cùng mình nhé!")
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài hát qua..."))
        }

        btnDownload.setOnClickListener {
            if (!isLoggedIn) {
                Toast.makeText(this, "Vui lòng đăng nhập để tải nhạc!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val resID = resources.getIdentifier(songPath, "raw", packageName)
                if (resID == 0) return@setOnClickListener

                val inputStream = resources.openRawResource(resID)
                val fileName = "$songTitle - $songArtist.mp3"
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                val outputStream = FileOutputStream(file)

                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                Toast.makeText(this, "Đã tải xong! Kiểm tra trong thư mục Download.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi tải xuống: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPlayer(resID: Int) {
        try {
            mediaPlayer = MediaPlayer.create(this, resID)
            seekBar.max = mediaPlayer.duration
            tvTotalTime.text = createTimeLabel(mediaPlayer.duration)
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khi tải file nhạc!", Toast.LENGTH_SHORT).show()
            return
        }

        btnPlayPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)

                // === PHẦN MỚI THÊM: TẠM DỪNG HIỆU ỨNG XOAY ===
                albumAnimator?.pause()
                // ============================================
            } else {
                mediaPlayer.start()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                updateSeekBar()

                // === PHẦN MỚI THÊM: BẮT ĐẦU/HOẠT ĐỘNG LẠI HIỆU ỨNG XOAY ===
                if (albumAnimator?.isStarted == true) {
                    albumAnimator?.resume() // Tiếp tục nếu đã bắt đầu trước đó
                } else {
                    albumAnimator?.start() // Bắt đầu mới
                }
                // =========================================================
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = createTimeLabel(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { handler.removeCallbacksAndMessages(null) }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (this@PlayerActivity::mediaPlayer.isInitialized) {
                    mediaPlayer.seekTo(seekBar!!.progress)
                    updateSeekBar()
                }
            }
        })
    }

    private fun updateSeekBar() {
        if (mediaPlayer.isPlaying) {
            val currentPos = mediaPlayer.currentPosition
            if (!isLoggedIn && currentPos >= 30000) {
                mediaPlayer.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)

                // === PHẦN MỚI THÊM: TẠM DỪNG HIỆU ỨNG XOAY ===
                albumAnimator?.pause()
                // ============================================

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
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)

            // === PHẦN MỚI THÊM: TẠM DỪNG HIỆU ỨNG XOAY ===
            albumAnimator?.pause()
            // ============================================
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
            handler.removeCallbacksAndMessages(null)

            // === PHẦN MỚI THÊM: HỦY HIỆU ỨNG XOAY ===
            albumAnimator?.cancel()
            // ========================================
        }
    }
}