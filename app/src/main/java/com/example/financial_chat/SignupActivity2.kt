package com.example.financial_chat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.financial_chat.databinding.ActivitySignupBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import at.favre.lib.crypto.bcrypt.BCrypt

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document

class SignupActivity2 : AppCompatActivity(){
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

            // MongoDB에 사용자 등록
            val isSuccess = registerUser(email, password, name, userId)

            if (isSuccess) {
                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                // 회원가입 성공 후 원하는 동작 수행 (예: 로그인 화면으로 이동)
                finish()
            } else {
                Toast.makeText(this, "User already exists or an error occurred.", Toast.LENGTH_SHORT).show()
            }
        }

        // ID 중복 체크
        binding.signupIdCheckTxt.setOnClickListener {
            val userId = binding.signupId.text.toString().trim()
            if (userId.isEmpty()) {
                Toast.makeText(this, "Please enter an ID to check", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val isIdAvailable = checkUserIdAvailability(userId)
            if (isIdAvailable) {
                // ID가 사용 가능하면 이미지뷰 배경 변경
                binding.signupIdCheck.setBackgroundResource(R.drawable.circle_purp2)
                Toast.makeText(this, "ID is available", Toast.LENGTH_SHORT).show()
            } else {
                // ID가 중복이면 배경 유지 또는 변경
                binding.signupIdCheck.setBackgroundResource(R.drawable.circle_gray3)
                Toast.makeText(this, "ID is already taken", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 회원가입 메서드
    fun registerUser(email: String, password: String, name: String, id: String): Boolean {
        val client = MongoClients.create("mongodb+srv://kjehy2001:kjhghqkr1024!@cluster0.mongodb.net/financial_chat?retryWrites=true&w=majority")
        val database = client.getDatabase("financial_chat")
        val collection: MongoCollection<Document> = database.getCollection("users")

        try {
            // 중복 이메일 체크
            val existingEmail = collection.find(Document("email", email)).first()
            if (existingEmail != null) {
                println("Email already exists.")
                return false // 이미 존재하는 이메일
            }

            // 중복 ID 체크 (checkUserIdAvailability 호출)
            if (!checkUserIdAvailability(id)) {
                println("ID already exists.")
                return false // 이미 존재하는 ID
            }

            // 사용자 삽입
            val newUser = Document("email", email)
                .append("password", password) // 비밀번호 해시 처리 권장
                .append("name", name)
                .append("id", id)

            collection.insertOne(newUser)
            println("User registered successfully.")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            client.close()
        }
    }

    // ID 중복 체크 메서드
    private fun checkUserIdAvailability(userId: String): Boolean {
        val client = MongoClients.create("your_mongodb_connection_string")
        val database = client.getDatabase("financial_chat")
        val collection: MongoCollection<Document> = database.getCollection("users")

        return try {
            val existingUser = collection.find(Document("id", userId)).first()
            existingUser == null // 사용자가 없으면 true (사용 가능)
        } catch (e: Exception) {
            e.printStackTrace()
            false // 오류 발생 시 false 반환
        } finally {
            client.close() // 연결을 항상 종료
        }
    }
}