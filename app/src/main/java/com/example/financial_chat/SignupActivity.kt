package com.example.financial_chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financial_chat.databinding.ActivitySignupBinding
//import com.mongodb.client.MongoClients
//import com.mongodb.client.MongoCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import org.bson.Document
//import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding // View Binding 객체 선언
    private lateinit var auth: FirebaseAuth // Firebase Authentication
    private lateinit var firestore: FirebaseFirestore // Firebase Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 회원가입 버튼 클릭 이벤트
        binding.btnJoinUs.setOnClickListener {
            val name = binding.signupName.text.toString().trim()
            val email = binding.signupEmail.text.toString().trim()
            val userId = binding.signupId.text.toString().trim()
            val password = binding.signupPw.text.toString().trim()
            val age = binding.signupAge.text.toString().trim()
            val job = binding.signupJob.text.toString().trim()
            val interest = binding.signupInterest.text.toString().trim()
            val goal = binding.signupGoal.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || userId.isEmpty() || password.isEmpty() ||
                age.isEmpty() || job.isEmpty() || interest.isEmpty() || goal.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 백그라운드에서 회원가입 처리
            CoroutineScope(Dispatchers.IO).launch {
                val isIdAvailable = checkUserIdAvailability(userId)
                withContext(Dispatchers.Main) {
                    if (!isIdAvailable) {
                        Toast.makeText(this@SignupActivity, "ID is already taken.", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }
                }

                registerUser(email, password, name, userId, age, job, interest, goal)
            }
        }

        // ID 중복 체크
        binding.signupIdCheckTxt.setOnClickListener {
            val userId = binding.signupId.text.toString().trim()
            if (userId.isEmpty()) {
                Toast.makeText(this, "Please enter an ID to check", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                val isIdAvailable = checkUserIdAvailability(userId)
                withContext(Dispatchers.Main) {
                    if (isIdAvailable) {
                        binding.signupIdCheck.setBackgroundResource(R.drawable.circle_purp2)
                        Toast.makeText(this@SignupActivity, "ID is available", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.signupIdCheck.setBackgroundResource(R.drawable.circle_gray3)
                        Toast.makeText(this@SignupActivity, "ID is already taken", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 회원가입 메서드
    private fun registerUser(
        email: String,
        password: String,
        name: String,
        userId: String,
        age: String,
        job: String,
        interest: String,
        goal: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "id" to userId,
                            "age" to age,
                            "job" to job,
                            "interest" to interest,
                            "goal" to goal
                        )

                        firestore.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT)
                                    .show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error saving user: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Error creating user: User ID is null.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Signup failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // ID 중복 체크 메서드
    private suspend fun checkUserIdAvailability(userId: String): Boolean {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("id", userId)
                .get()
                .await()

            snapshot.isEmpty // true면 사용 가능, false면 이미 존재
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
