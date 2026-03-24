package com.example.app_music

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "AppMusicDB", null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableUser = "CREATE TABLE Users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, role INTEGER)"
        val createTableSong = "CREATE TABLE Songs (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, artist TEXT, path TEXT, image TEXT, lyrics TEXT, likes INTEGER DEFAULT 0)"

        db?.execSQL(createTableUser)
        db?.execSQL(createTableSong)

        db?.execSQL("INSERT INTO Users (username, password, role) VALUES ('admin', 'admin123', 1)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Users")
        db?.execSQL("DROP TABLE IF EXISTS Songs")
        onCreate(db)
    }

    fun registerUser(username: String, pass: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("password", pass)
            put("role", 0)
        }
        val result = db.insert("Users", null, values)
        return result != -1L
    }

    fun checkUser(username: String, pass: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT role FROM Users WHERE username=? AND password=?", arrayOf(username, pass))
        var role = -1
        if (cursor.moveToFirst()) {
            role = cursor.getInt(0)
        }
        cursor.close()
        return role
    }

    fun addSong(title: String, artist: String, path: String, image: String, lyrics: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("artist", artist)
            put("path", path)
            put("image", image)
            put("lyrics", lyrics)
        }
        return db.insert("Songs", null, values)
    }

    @android.annotation.SuppressLint("Range")
    fun getAllSongs(): ArrayList<Song> {
        val songList = ArrayList<Song>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Songs", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val title = cursor.getString(cursor.getColumnIndex("title"))
                val artist = cursor.getString(cursor.getColumnIndex("artist"))
                val path = cursor.getString(cursor.getColumnIndex("path"))
                val image = cursor.getString(cursor.getColumnIndex("image"))
                val lyrics = cursor.getString(cursor.getColumnIndex("lyrics"))
                val likes = cursor.getInt(cursor.getColumnIndex("likes"))

                // ĐÃ SỬA: Thêm .toString() vào biến id
                songList.add(Song(id.toString(), title, artist, path, image, lyrics, likes))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return songList
    }

    fun updateSong(id: String, title: String, artist: String, path: String, image: String, lyrics: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("artist", artist)
            put("path", path)
            put("image", image)
            put("lyrics", lyrics)
        }
        val result = db.update("Songs", values, "id=?", arrayOf(id))
        return result > 0
    }

    fun deleteSong(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete("Songs", "id=?", arrayOf(id))
        return result > 0
    }

    @android.annotation.SuppressLint("Range")
    fun getTopLikedSongs(): ArrayList<Song> {
        val songList = ArrayList<Song>()
        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM Songs WHERE likes >= 2 ORDER BY likes DESC LIMIT 5", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val title = cursor.getString(cursor.getColumnIndex("title"))
                val artist = cursor.getString(cursor.getColumnIndex("artist"))
                val path = cursor.getString(cursor.getColumnIndex("path"))
                val image = cursor.getString(cursor.getColumnIndex("image"))
                val lyrics = cursor.getString(cursor.getColumnIndex("lyrics"))
                val likes = cursor.getInt(cursor.getColumnIndex("likes"))

                // ĐÃ SỬA: Thêm .toString() vào biến id
                songList.add(Song(id.toString(), title, artist, path, image, lyrics, likes))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return songList
    }

    fun updateLike(songId: Int, newLikeCount: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("likes", newLikeCount)
        }
        val result = db.update("Songs", values, "id=?", arrayOf(songId.toString()))
        return result > 0
    }

    fun checkUsername(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Users WHERE username=?", arrayOf(username))
        val count = cursor.count
        cursor.close()
        return count > 0
    }
}