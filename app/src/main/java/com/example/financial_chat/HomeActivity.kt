package com.example.financial_chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financial_chat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.mongodb.client.MongoClients
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//import org.bson.Document

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // SessionManager 초기화
        sessionManager = SessionManager(this)

        // 로그인 상태 확인
        val userId = auth.currentUser?.uid
        if (userId == null) {
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
                    binding.txtUserName.text = userData["name"] as String?
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 버튼 클릭 이벤트 처리
        setupButtonListeners(userId)
    }

    private fun setupButtonListeners(userId: String) {
        // 새 채팅방 생성 버튼
        binding.btnStartChat.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val newRoomId = createNewChatRoom(userId)
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                    intent.putExtra("ROOM_ID", newRoomId)
                    startActivity(intent)
                }
            }
        }

        // 최근 채팅방으로 이동 버튼
        binding.btnChat.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val recentRoomId = fetchMostRecentChatRoom(userId)
                withContext(Dispatchers.Main) {
                    if (recentRoomId != null) {
                        val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                        intent.putExtra("ROOM_ID", recentRoomId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@HomeActivity, "채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 마이페이지 버튼
        binding.btnMyPage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            intent.putExtra("USER_EMAIL", sessionManager.getUserEmail())
            startActivity(intent)
        }
    }

    // 새로운 채팅방 생성
    private suspend fun createNewChatRoom(userId: String): String {
        val timestamp = System.currentTimeMillis()
        val roomName = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
        val newChatRoom = mapOf(
            "participants" to listOf(userId, "bot"),
            "createdAt" to timestamp,
            "roomName" to roomName
        )

        return try {
            val roomId = firestore.collection("chatrooms").add(newChatRoom).await().id
            Log.d("CREATE_CHATROOM", "Created new chatroom: $roomId with name $roomName")

            // 예약 삭제 로직
            CoroutineScope(Dispatchers.IO).launch {
                delay(120000)
                checkAndDeleteEmptyRoom(roomId)
            }

            roomId
        } catch (e: Exception) {
            Log.e("CREATE_CHATROOM", "Failed to create new chatroom: ${e.message}")
            throw e
        }
    }

    private suspend fun checkAndDeleteEmptyRoom(roomId: String) {
        val messages = firestore.collection("chatrooms").document(roomId)
            .collection("messages").get().await()
        if (messages.isEmpty) {
            firestore.collection("chatrooms").document(roomId).delete().await()
        }
    }


    // 가장 최근 생성된 채팅방 가져오기
    private suspend fun fetchMostRecentChatRoom(userId: String): String? {
        return try {
            val chatRooms = firestore.collection("chatrooms")
                .whereArrayContains("participants", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            // 로그로 확인
            Log.d("FETCH_CHATROOM", "Fetched chatRooms: ${chatRooms.documents}")

            if (chatRooms.documents.isNotEmpty()) {
                chatRooms.documents[0].id // 가장 최근 채팅방의 ID 반환
            } else {
                null // 채팅방이 없으면 null 반환
            }
        } catch (e: Exception) {
            Log.e("FETCH_CHATROOM", "Error fetching chatrooms: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /*
    // 버튼 클릭 이벤트 설정 (1st)
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

     */

    // Firestore에서 유저 데이터 가져오기
    private suspend fun getUserData(userId: String): Map<String, Any>? {
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