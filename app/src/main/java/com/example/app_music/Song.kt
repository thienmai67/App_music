package com.example.app_music // Đổi thành package của bạn

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val path: String,
    val image: String,
    val lyrics: String,
    var likes: Int = 0
)