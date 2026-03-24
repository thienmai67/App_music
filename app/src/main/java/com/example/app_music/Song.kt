package com.example.app_music

data class Song(
    var id: String = "",
    var title: String = "",
    var artist: String = "",
    var path: String = "",
    var image: String = "",
    var lyrics: String = "",
    var likes: Int = 0
)