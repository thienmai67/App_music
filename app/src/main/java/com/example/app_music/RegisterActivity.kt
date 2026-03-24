package com.example.app_music

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    // 1. Khai báo biến Firebase Auth thay cho DBHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 2. Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        val edtRegUsername = findViewById<EditText>(R.id.edtRegUsername)
        val edtRegPassword = findViewById<EditText>(R.id.edtRegPassword)
        val edtRegConfirmPassword = findViewById<EditText>(R.id.edtRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val user = edtRegUsername.text.toString().trim()
            val pass = edtRegPassword.text.toString().trim()
            val confirmPass = edtRegConfirmPassword.text.toString().trim()

            // Kiểm tra không được để trống
            if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu có khớp nhau không
            if (pass != confirmPass) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // MỚI: Firebase yêu cầu mật khẩu phải từ 6 ký tự trở lên
            if (pass.length < 6) {
                Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // MỚI: Mẹo biến Username thành Email để Firebase chấp nhận mà không cần sửa giao diện XML
            val email = if (user.contains("@")) user else "$user@appmusic.com"

            // 3. Thực hiện lưu vào Firebase Authentication thay vì SQLite
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Thành công
                        Toast.makeText(this, "Đăng ký thành công! Mời đăng nhập.", Toast.LENGTH_SHORT).show()
                        // Đóng Activity Đăng ký và quay lại màn hình Đăng nhập (LoginActivity)
                        finish()
                    } else {
                        // Thất bại (Firebase sẽ tự bắt lỗi trùng tài khoản hoặc lỗi mạng)
                        Toast.makeText(this, "Đăng ký thất bại: Tên Tài khoản Đã Tồn Tại Hoặc Lỗi Mạng!", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Nếu bấm "Đã có tài khoản", đóng trang này lại để về lại trang Login
        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}