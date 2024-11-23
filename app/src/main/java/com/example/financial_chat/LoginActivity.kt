package com.example.financial_chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financial_chat.databinding.ActivityLoginBinding
import com.mongodb.client.MongoClients
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bson.Document
import at.favre.lib.crypto.bcrypt.BCrypt

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding // View Binding 객체 선언
    private lateinit var sessionManager: SessionManager // 로그인 상태 관리 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SessionManager 초기화
        sessionManager = SessionManager(this)

        // 로그인 버튼 클릭 이벤트
        binding.btnLogin.setOnClickListener {
            val email = binding.loginId.text.toString().trim()
            val password = binding.loginPw.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 백그라운드에서 로그인 처리
            CoroutineScope(Dispatchers.IO).launch {
                val user = loginUser(email, password)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        // 로그인 성공, 세션 저장
                        sessionManager.saveUserSession(user.getString("email"), user.getString("id"))
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        // 메인 화면으로 이동
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // "Join Us" 버튼 클릭 이벤트
        binding.joinUs.setOnClickListener {
            // 회원가입 화면으로 이동
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // "Forgot Password?" 버튼 클릭 이벤트
        binding.btnFindPwd.setOnClickListener {
            Toast.makeText(this, "Forgot Password? feature is not implemented yet.", Toast.LENGTH_SHORT).show()
        }
    }

    // 로그인 메서드
    private fun loginUser(email: String, password: String): Document? {
        MongoClients.create("your_mongodb_connection_string").use { client ->
            val database = client.getDatabase("users") // 데이터베이스 이름
            val collection = database.getCollection("users") // 컬렉션 이름

            return try {
                // 사용자 검색
                val user = collection.find(Document("email", email)).first()
                if (user != null) {
                    val storedHashedPassword = user.getString("password")
                    // 비밀번호 검증
                    val result = BCrypt.verifyer().verify(password.toCharArray(), storedHashedPassword)
                    if (result.verified) user else null
                } else {
                    null // 사용자 없음
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
