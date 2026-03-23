package com.example.app_music

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE)
        val checkIsLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (checkIsLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        dbHelper = DBHelper(this)

        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        val tvSkipLogin = findViewById<TextView>(R.id.tvSkipLogin)

        btnLogin.setOnClickListener {
            val user = edtUsername.text.toString().trim()
            val pass = edtPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val role = dbHelper.checkUser(user, pass)

            when (role) {
                1 -> {
                    // ĐÃ SỬA: Lưu trạng thái là Admin (role = 1) và cho vào MainActivity
                    saveLoginState(true, 1)
                    Toast.makeText(this, "Xin chào Quản trị viên!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                0 -> {
                    // ĐÃ SỬA: Lưu trạng thái là User thường (role = 0)
                    saveLoginState(true, 0)
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else -> {
                    Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvSkipLogin.setOnClickListener {
            // Khách vãng lai: Lưu role = -1
            saveLoginState(false, -1)
            Toast.makeText(this, "Đang vào chế độ Khách...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // ĐÃ SỬA: Hàm này giờ lưu thêm quyền (role)
    private fun saveLoginState(isLoggedIn: Boolean, role: Int) {
        val sharedPref = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", isLoggedIn)
            putInt("role", role) // Lưu thêm biến role vào bộ nhớ
            apply()
        }
    }
}