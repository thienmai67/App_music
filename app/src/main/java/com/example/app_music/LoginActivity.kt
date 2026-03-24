package com.example.app_music

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // 1. Khai báo biến Firebase Auth thay cho DBHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Kiểm tra xem đã đăng nhập từ trước chưa
        val sharedPref = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE)
        val checkIsLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (checkIsLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // 2. Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

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

            // Mẹo biến Username thành Email (giống bên Đăng ký)
            val email = if (user.contains("@")) user else "$user@appmusic.com"

            // 3. Đăng nhập bằng Firebase
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Xử lý phân quyền (Tài khoản tên 'admin' thì làm quản trị viên)
                        if (user == "admin" || email == "admin@appmusic.com") {
                            saveLoginState(true, 1)
                            Toast.makeText(this, "Xin chào Quản trị viên!", Toast.LENGTH_SHORT).show()
                        } else {
                            saveLoginState(true, 0)
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        }

                        // Chuyển sang màn hình chính
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
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

    // Hàm lưu trạng thái đăng nhập
    private fun saveLoginState(isLoggedIn: Boolean, role: Int) {
        val sharedPref = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", isLoggedIn)
            putInt("role", role)
            apply()
        }
    }
}