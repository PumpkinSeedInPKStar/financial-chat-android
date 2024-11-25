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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding // View Binding 객체 선언
    private lateinit var sessionManager: SessionManager // 로그인 상태 관리 객체
    private lateinit var auth: FirebaseAuth // Firebase Authentication
    private lateinit var firestore: FirebaseFirestore // Firebase Firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

            // Firebase Authentication으로 로그인
            loginUser(email, password)
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
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Firebase Firestore에서 사용자 정보 가져오기
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val user = fetchUserDetails(userId)
                            withContext(Dispatchers.Main) {
                                if (user != null) {
                                    // 세션 저장
                                    sessionManager.saveUserSession(user["email"] as String, userId)
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // 메인 화면으로 이동
                                    val intent =
                                        Intent(this@LoginActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "User data not found.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Login failed: User ID not found.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Firestore에서 사용자 정보 가져오기
    private suspend fun fetchUserDetails(userId: String): Map<String, Any>? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                document.data
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
