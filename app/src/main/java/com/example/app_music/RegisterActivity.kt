package com.example.app_music

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DBHelper(this)

        val edtRegUsername = findViewById<EditText>(R.id.edtRegUsername)
        val edtRegPassword = findViewById<EditText>(R.id.edtRegPassword)
        val edtRegConfirmPassword = findViewById<EditText>(R.id.edtRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val user = edtRegUsername.text.toString().trim()
            val pass = edtRegPassword.text.toString().trim()
            val confirmPass = edtRegConfirmPassword.text.toString().trim()

            // 1. Kiểm tra không được để trống
            if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Kiểm tra mật khẩu có khớp nhau không
            if (pass != confirmPass) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === PHẦN MỚI THÊM: KIỂM TRA TRÙNG LẶP TÊN ĐĂNG NHẬP ===
            if (dbHelper.checkUsername(user)) {
                Toast.makeText(this, "Tên đăng nhập '$user' đã tồn tại! Vui lòng chọn tên khác.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // =======================================================

            // 3. Thực hiện lưu vào SQLite (Mặc định sẽ là User thường, role = 0)
            val isSuccess = dbHelper.registerUser(user, pass)

            if (isSuccess) {
                Toast.makeText(this, "Đăng ký thành công! Mời đăng nhập.", Toast.LENGTH_SHORT).show()
                // Đóng Activity Đăng ký và quay lại màn hình Đăng nhập (LoginActivity)
                finish()
            } else {
                Toast.makeText(this, "Đăng ký thất bại do lỗi hệ thống!", Toast.LENGTH_SHORT).show()
            }
        }

        // Nếu bấm "Đã có tài khoản", chỉ cần đóng trang này lại để về lại trang Login
        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}