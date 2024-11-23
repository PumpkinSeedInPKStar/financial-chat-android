package com.example.financial_chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financial_chat.databinding.ActivitySignupBinding
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bson.Document
import at.favre.lib.crypto.bcrypt.BCrypt

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding // View Binding 객체 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 회원가입 버튼 클릭 이벤트
        binding.btnJoinUs.setOnClickListener {
            val name = binding.signupName.text.toString().trim()
            val email = binding.signupEmail.text.toString().trim()
            val userId = binding.signupId.text.toString().trim()
            val password = binding.signupPw.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 백그라운드에서 회원가입 처리
            CoroutineScope(Dispatchers.IO).launch {
                val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                val isSuccess = registerUser(email, hashedPassword, name, userId)
                withContext(Dispatchers.Main) {
                    if (isSuccess) {
                        Toast.makeText(this@SignupActivity, "Signup successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignupActivity, "User already exists or an error occurred.", Toast.LENGTH_SHORT).show()
                    }
                }
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
    private fun registerUser(email: String, password: String, name: String, id: String): Boolean {
        MongoClients.create("mongodb+srv://kjehy2001:kjhghqkr1024!@cluster0.mongodb.net/users?retryWrites=true&w=majority").use { client ->
            val database = client.getDatabase("users")
            val collection: MongoCollection<Document> = database.getCollection("users")
            return try {
                val existingEmail = collection.find(Document("email", email)).first()
                if (existingEmail != null) return false

                val existingId = collection.find(Document("id", id)).first()
                if (existingId != null) return false

                val newUser = Document("email", email)
                    .append("password", password)
                    .append("name", name)
                    .append("id", id)

                collection.insertOne(newUser)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // ID 중복 체크 메서드
    private fun checkUserIdAvailability(userId: String): Boolean {
        MongoClients.create("your_mongodb_connection_string").use { client ->
            val database = client.getDatabase("financial_chat")
            val collection: MongoCollection<Document> = database.getCollection("users")
            return try {
                val existingUser = collection.find(Document("id", userId)).first()
                existingUser == null
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
