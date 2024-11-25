package com.example.financial_chat

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.financial_chat.databinding.ActivityMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MypageActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMypageBinding // View Binding
    private lateinit var auth: FirebaseAuth // Firebase Authentication
    private lateinit var firestore: FirebaseFirestore // Firestore Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 유저 데이터 가져오기
        loadUserData()

        // 버튼 클릭 이벤트 설정
        setupButtonListeners()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore에서 유저 데이터 가져오기
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDocument = firestore.collection("users").document(userId).get().await()
                if (userDocument.exists()) {
                    val name = userDocument.getString("name") ?: "N/A"
                    val email = userDocument.getString("email") ?: "N/A"
                    val age = userDocument.getString("age") ?: "N/A"
                    val job = userDocument.getString("job") ?: "N/A"
                    val interest = userDocument.getString("interest") ?: "N/A"
                    val goal = userDocument.getString("goal") ?: "N/A"

                    withContext(Dispatchers.Main) {
                        // 데이터를 뷰에 표시
                        binding.txtMypageName.text = name
                        binding.txtMypageEmail.text = email
                        binding.txtAge.text = age
                        binding.txtJob.text = job
                        binding.txtInterest.text = interest
                        binding.txtGoal.text = goal
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MypageActivity, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MypageActivity, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupButtonListeners() {
        // 홈 버튼 클릭 시 홈 화면으로 이동
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 채팅 버튼 클릭 시 채팅 화면으로 이동
        binding.btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}