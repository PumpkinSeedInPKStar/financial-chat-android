package com.example.financial_chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financial_chat.databinding.ActivityMainBinding
import com.mongodb.client.MongoClients
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bson.Document

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SessionManager 초기화
        sessionManager = SessionManager(this)

        // 로그인 상태 확인
        val userId = sessionManager.getUserId()
        val userEmail = sessionManager.getUserEmail()
        if (userId == null || userEmail == null) {
            // 로그인 상태가 아니면 로그인 화면으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 유저 데이터 로드 및 UI 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            val userData = getUserData(userId)
            withContext(Dispatchers.Main) {
                if (userData != null) {
                    // 유저 이름 표시
                    binding.txtUserName.text = userData.getString("name")
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 버튼 클릭 이벤트 처리
        setupButtonListeners()
    }

    // 버튼 클릭 이벤트 설정
    private fun setupButtonListeners() {
        // 상담하러 가기 버튼
        binding.btnStartChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            intent.putExtra("USER_EMAIL", sessionManager.getUserEmail())
            startActivity(intent)
        }

        // 채팅 버튼
        binding.btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            intent.putExtra("USER_EMAIL", sessionManager.getUserEmail())
            startActivity(intent)
        }

        // 마이페이지 버튼
        binding.btnMyPage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            intent.putExtra("USER_EMAIL", sessionManager.getUserEmail())
            startActivity(intent)
        }
    }

    // MongoDB에서 유저 데이터 가져오기
    private fun getUserData(userId: String): Document? {
        MongoClients.create("your_mongodb_connection_string").use { client ->
            val database = client.getDatabase("users")
            val collection = database.getCollection("users")

            return try {
                collection.find(Document("id", userId)).first()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}