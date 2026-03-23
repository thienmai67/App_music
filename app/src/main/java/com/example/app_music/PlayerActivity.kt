package com.example.app_music

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    private lateinit var imgAlbumArt: ImageView
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
    private var songImage = ""
    private var likeCount = 0
    private var songLocalPath: String? = null

    private var albumAnimator: ObjectAnimator? = null

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
        songId = intent.getIntExtra("song_id", -1)
        songTitle = intent.getStringExtra("SONG_TITLE") ?: "Tên bài hát"
        songArtist = intent.getStringExtra("SONG_ARTIST") ?: "Ca sĩ"
        songPath = intent.getStringExtra("SONG_PATH") ?: "test_song"

        // Nhận đường dẫn nhạc và ảnh local (nếu phát từ thư mục Tải về)
        songLocalPath = intent.getStringExtra("SONG_LOCAL_PATH")
        val songLocalImage = intent.getStringExtra("SONG_LOCAL_IMAGE")

        songImage = intent.getStringExtra("SONG_IMAGE") ?: "ic_menu_gallery"
        songLyrics = intent.getStringExtra("SONG_LYRICS") ?: "Chưa có lời bài hát"
        likeCount = intent.getIntExtra("song_likes", 0)

        tvSongTitle.text = songTitle
        tvArtistName.text = songArtist
        updateLikeUI()

        // --- ƯU TIÊN HIỂN THỊ ẢNH LOCAL NẾU CÓ ---
        if (!songLocalImage.isNullOrEmpty() && File(songLocalImage).exists()) {
            val bitmap = BitmapFactory.decodeFile(songLocalImage)
            imgAlbumArt.setImageBitmap(bitmap)
        } else {
            val imageResId = resources.getIdentifier(songImage, "drawable", packageName)
            if (imageResId != 0) {
                imgAlbumArt.setImageResource(imageResId)
            } else {
                imgAlbumArt.setImageResource(android.R.drawable.ic_menu_gallery)
            }
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
            // Sử dụng thư mục riêng của ứng dụng (App-specific storage)

            if (!isLoggedIn) {
                Toast.makeText(this, "Vui lòng đăng nhập để tải nhạc!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!songLocalPath.isNullOrEmpty()) {
                Toast.makeText(this, "Bài hát này đã có sẵn trong máy!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resID = resources.getIdentifier(songPath, "raw", packageName)
            if (resID == 0) {
                Toast.makeText(this, "Lỗi: Không tìm thấy file gốc (res/raw) để tải!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // TẠO THƯ MỤC LƯU CHUNG
                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                val musicDir = File(downloadsDir, "AppMusic")
                if (!musicDir.exists()) {
                    musicDir.mkdirs()
                }

                // TÊN FILE CƠ BẢN (KHÔNG ĐUÔI)
                val baseFileName = "$songTitle - $songArtist"

                // 1. TẢI FILE NHẠC (.mp3)
                val mp3InputStream = resources.openRawResource(resID)
                val mp3File = File(musicDir, "$baseFileName.mp3")
                val mp3OutputStream = FileOutputStream(mp3File)
                mp3InputStream.copyTo(mp3OutputStream)
                mp3InputStream.close()
                mp3OutputStream.close()

                // 2. TẢI ĐỒNG THỜI FILE ẢNH BÌA (.jpg)
                downloadArtwork(songImage, baseFileName, musicDir)

                Toast.makeText(this, "Đã tải xong nhạc!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi tải xuống: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // HÀM TẢI VÀ LƯU ẢNH BÌA TỪ DRAWABLE SANG BỘ NHỚ
    private fun downloadArtwork(imageName: String, baseFileName: String, targetDir: File) {
        val imageResId = resources.getIdentifier(imageName, "drawable", packageName)
        val finalImageResId = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery

        try {
            val bitmap = BitmapFactory.decodeResource(resources, finalImageResId)

            if (bitmap != null) {
                val imageFile = File(targetDir, "$baseFileName.jpg")
                val outputStream = FileOutputStream(imageFile)

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                outputStream.flush()
                outputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupPlayer(resID: Int) {
        try {
            if (!songLocalPath.isNullOrEmpty() && File(songLocalPath!!).exists()) {
                mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(songLocalPath)
                mediaPlayer.prepare()
            } else {
                mediaPlayer = MediaPlayer.create(this, resID)
            }

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
                albumAnimator?.pause()
            } else {
                mediaPlayer.start()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                updateSeekBar()

                if (albumAnimator?.isStarted == true) {
                    albumAnimator?.resume()
                } else {
                    albumAnimator?.start()
                }
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
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            albumAnimator?.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
            handler.removeCallbacksAndMessages(null)
            albumAnimator?.cancel()
        }
    }
}