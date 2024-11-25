package com.example.financial_chat
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financial_chat.databinding.ActivityChat2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.bson.Document

class ChatActivity : AppCompatActivity(){
    private lateinit var binding: ActivityChat2Binding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRoomAdapter: ChatRoomAdapter

    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var currentRoomId: String? = null // 현재 선택된 채팅방 ID
    private val db = FirebaseFirestore.getInstance() // Firestore 인스턴스


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChat2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 로그인 상태 확인
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // 로그인 상태가 아니면 로그인 화면으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 사이드바 기본 상태 닫기
        binding.drawerLayout.closeDrawer(GravityCompat.START, false)

        // Initialize chat RecyclerView
        chatAdapter = ChatAdapter(emptyList())
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        // 사이드바 RecyclerView 설정
        chatRoomAdapter = ChatRoomAdapter(mutableListOf()) { chatRoom ->
            loadChatRoom(chatRoom)
        }
//        binding.navigationView.findViewById<RecyclerView>(R.id.chat_room_list).apply {
        binding.navigationView.findViewById<RecyclerView>(R.id.chat_room_list).apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatRoomAdapter
        }

        // Load chat rooms from MongoDB
        loadChatRooms(userId)

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.sendBtn.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty() && currentRoomId != null) {
                sendMessage(message)
                binding.messageEditText.text.clear()
            } else {
                Toast.makeText(this, "채팅방을 선택하거나 메시지를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 마이페이지 버튼
        binding.btnMyPage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            intent.putExtra("USER_EMAIL", sessionManager.getUserEmail())
            startActivity(intent)
        }

        // 홈 버튼
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            intent.putExtra("USER_EMAIL", sessionManager.getUserEmail())
            startActivity(intent)
        }
    }

    private fun loadChatRooms(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val chatRooms = fetchChatRooms(userId)
            if (chatRooms.isEmpty()) {
                createDefaultChatRoom(userId)
            } else {
                withContext(Dispatchers.Main) {
                    chatRoomAdapter.updateChatRooms(chatRooms)
                }
            }
        }
    }

    // 채팅방 생성
    private suspend fun createDefaultChatRoom(userId: String) {
        val defaultRoom = mapOf(
            "participants" to listOf(userId, "bot"),
            "messages" to emptyList<Map<String, Any>>()
        )
        val roomId = firestore.collection("chatrooms").add(defaultRoom).await().id
        currentRoomId = roomId
        withContext(Dispatchers.Main) {
            val newChatRoom = ChatRoom(roomId, listOf(userId, "bot"), emptyList())
            chatRoomAdapter.addChatRoom(newChatRoom)
        }
    }

    // 특정 채팅방 로드
    private fun loadChatRoom(chatRoom: ChatRoom) {
        currentRoomId = chatRoom.roomId // 현재 채팅방 ID 업데이트
        title = "Chat Room: ${chatRoom.roomId}" // 현재 채팅방 표시
        chatAdapter.updateMessages(chatRoom.messages) // 메시지 RecyclerView 업데이트
        binding.drawerLayout.closeDrawer(GravityCompat.START) // 사이드바 닫기
    }

    // 메시지 전송
    private fun sendMessage(message: String) {
        val newMessage = ChatMessage("user", message, System.currentTimeMillis().toString())
        chatAdapter.addMessage(newMessage) // 메시지 추가

        // MongoDB에 메시지 저장 및 챗봇 응답 처리
        CoroutineScope(Dispatchers.IO).launch {
            currentRoomId?.let { roomId ->
                try {
                    // Firebase Firestore에 메시지 저장
                    saveMessageToFirebase(roomId, newMessage)
                    // 챗봇 응답 추가
                    val botReply = getBotReply(message)
                    val botMessage = ChatMessage("bot", botReply, System.currentTimeMillis().toString())
                    saveMessageToFirebase(roomId, botMessage)

                    withContext(Dispatchers.Main) {
                        chatAdapter.addMessage(botMessage)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // MongoDB에서 메시지 저장
    private suspend fun saveMessageToFirebase(roomId: String, message: ChatMessage) {
        val messageData = mapOf(
            "sender" to message.sender,
            "message" to message.message,
            "timestamp" to message.timestamp
        )

        db.collection("chatrooms")
            .document(roomId)
            .collection("messages")
            .add(messageData)
            .await()
    }

    private suspend fun fetchChatRooms(roomId: String): List<ChatRoom> {
        return try {
            val chatRooms = db.collection("chatrooms")
                .whereArrayContains("participants", roomId)
                .get()
                .await()
                .documents.map { document ->
                    ChatRoom(
                        roomId = document.id,
                        participants = document["participants"] as List<String>,
                        messages = fetchMessagesFromFirebase(document.id)
                    )
                }
            chatRooms
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchMessagesFromFirebase(roomId: String): List<ChatMessage> {
        return try {
            db.collection("chatrooms").document(roomId).collection("messages")
                .get()
                .await()
                .documents.map { document ->
                    ChatMessage(
                        sender = document.getString("sender") ?: "Unknown",
                        message = document.getString("message") ?: "",
                        timestamp = document.getString("timestamp") ?: System.currentTimeMillis().toString()
                    )
                }
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error fetching messages: ${e.message}")
            emptyList()
        }
    }

    private fun getBotReply(userMessage: String): String {
        return "You said: $userMessage"
    }
}