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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
//import com.mongodb.client.MongoClients
//import com.mongodb.client.MongoCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
//import org.bson.Document
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity(){
    private lateinit var binding: ActivityChat2Binding
    private lateinit var chatAdapter: ChatAdapter
//    private lateinit var chatModel: ChatModel
    private lateinit var chatRoomAdapter: ChatRoomAdapter

    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var apiService: OllamaApiService

    private var currentRoomId: String? = null // 현재 선택된 채팅방 ID
    private val db = FirebaseFirestore.getInstance() // Firestore 인스턴스


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChat2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // sessionManager 초기화
        sessionManager = SessionManager(this)

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

        /*
        // ROOM_ID 가져오기
        currentRoomId = intent.getStringExtra("ROOM_ID")
        if (currentRoomId == null) {
            Toast.makeText(this, "채팅방을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
         */

        // ROOM_ID 가져오기
        currentRoomId = intent.getStringExtra("ROOM_ID")
        if (currentRoomId == null) {
            // ROOM_ID가 없는 경우 기존 채팅방 로드
            loadChatRooms(userId)
        } else {
            // 전달받은 ROOM_ID로 해당 채팅방 로드
            loadChatRoomMessages()
        }

        setupUI(userId)

        // 사이드바 기본 상태 닫기
        binding.drawerLayout.closeDrawer(GravityCompat.START, false)

        // 채팅방 목록 불러오기 및 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            val chatRooms = fetchChatRooms(auth.currentUser?.uid ?: return@launch)
            withContext(Dispatchers.Main) {
                if (chatRooms.isNotEmpty()) {
                    chatRoomAdapter.updateChatRooms(chatRooms)
                } else {
                    Toast.makeText(this@ChatActivity, "채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Initialize chat RecyclerView
        setupRecyclerView()

        // 사이드바 RecyclerView 설정
        chatRoomAdapter = ChatRoomAdapter(mutableListOf()) { chatRoom ->
            loadChatRoom(chatRoom)
        }

        binding.navigationView.findViewById<RecyclerView>(R.id.chat_room_list).apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatRoomAdapter
        }

        setupApiService()

        // Load chat rooms from MongoDB
//        loadChatRooms(userId)
    }

    private fun setupUI(userId: String) {
        setupRecyclerView()

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.sendBtn.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty() && currentRoomId != null) {
                handleUserMessage(message, userId)
                binding.messageEditText.text.clear()
            } else {
                Toast.makeText(this, "채팅방을 선택하거나 메시지를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMyPage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            intent.putExtra("USER_EMAIL", sessionManager.getUserEmail())
            startActivity(intent)
        }

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            intent.putExtra("USER_EMAIL", sessionManager.getUserEmail())
            startActivity(intent)
        }

        // 새 채팅방 생성 버튼
        binding.btnNewChat.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val newRoomId = createNewChatRoom(userId) // 새 채팅방 생성
                    withContext(Dispatchers.Main) {
                        currentRoomId = newRoomId // 새 채팅방 ID로 업데이트
                        loadChatRoomMessages() // 새 채팅방 메시지 로드
                        Toast.makeText(this@ChatActivity, "새 채팅방이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChatActivity, "채팅방 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(emptyList())
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }

    private fun setupApiService() {
        val gson = GsonBuilder()
            .setLenient() // 느슨한 JSON 파싱 허용
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:11434/") // Ollama API의 기본 URL, localhost 대신 10.0.2.2 사용
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(OllamaApiService::class.java)
    }

    private fun loadChatRooms(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
//            val userId = auth.currentUser?.uid ?: return@launch
            val chatRooms = fetchChatRooms(userId)
            if (chatRooms.isEmpty()) {
                createDefaultChatRoom(userId)
            } else {
                withContext(Dispatchers.Main) {
                    // 첫 번째 채팅방을 기본 선택
                    currentRoomId = chatRooms[0].roomId
                    loadChatRoom(chatRooms[0])
                    chatRoomAdapter.updateChatRooms(chatRooms)
                }
            }
        }
    }

    // 특정 채팅방 로드
    private fun loadChatRoom(chatRoom: ChatRoom) {
        currentRoomId = chatRoom.roomId // 현재 채팅방 ID 업데이트
        title = "Chat Room: ${chatRoom.roomId}" // 현재 채팅방 표시
        chatAdapter.updateMessage(chatRoom.messages)
//        chatAdapter.updateMessages(chatRoom.messages) // 메시지 RecyclerView 업데이트
        binding.drawerLayout.closeDrawer(GravityCompat.START) // 사이드바 닫기
    }

    private fun handleUserMessage(message: String, userId: String) {
        if (currentRoomId == null) {
            Toast.makeText(this, "채팅방을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val userMessage = ChatMessage("user", message, System.currentTimeMillis().toString())
        chatAdapter.addMessage(userMessage)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 유저 기본 정보 가져오기
                val userInfo = getUserBasicInfo(userId)

                // 유저의 질문과 기본 정보 조합
                val fullMessage = """
                User Info:
                $userInfo
                
                User Question:
                $message
            """.trimIndent()

                // Firestore에 유저 메시지 저장
                saveMessageToFirebase(currentRoomId!!, userMessage)

                val responseBody = apiService.generateChatCompletion(
                    ChatRequest("fc_gemma2", listOf(ChatContent("user", fullMessage)))
                )
                val responseStream = responseBody.byteStream().bufferedReader()

                val botMessageBuilder = StringBuilder()
                responseStream.useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            // JSON 파싱
                            val partialResponse = Gson().fromJson(line, PartialChatResponse::class.java)
                            val content = partialResponse.message.content

                            withContext(Dispatchers.Main) {
                                // 실시간으로 부분 응답 표시
                                if (content.isNotEmpty()) {
                                    botMessageBuilder.append(content)
                                    val partialBotMessage = ChatMessage("bot", botMessageBuilder.toString(), System.currentTimeMillis().toString())
                                    chatAdapter.updateLastBotMessage(partialBotMessage)
                                }
                            }

                            // 응답 완료 시 처리
                            if (partialResponse.done) {
                                val finalBotMessage = ChatMessage("bot", botMessageBuilder.toString(), System.currentTimeMillis().toString())

                                saveMessageToFirebase(currentRoomId!!, finalBotMessage)

                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
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

    /* // 1st
    private fun handleUserMessage(message: String, userId: String) {
        if (currentRoomId == null) {
            Toast.makeText(this, "채팅방을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val userMessage = ChatMessage("user", message, System.currentTimeMillis().toString())
        chatAdapter.addMessage(userMessage)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.generateChatCompletion(
                    ChatRequest("fc_gemma2", listOf(ChatContent("user", message)))
                )
                val botMessage = ChatMessage("bot", response.message.content, System.currentTimeMillis().toString())

                saveMessageToFirebase(currentRoomId ?: return@launch, userMessage)
                saveMessageToFirebase(currentRoomId ?: return@launch, botMessage)

//                saveMessageToFirebase(userMessage, userId)
//                saveMessageToFirebase(botMessage, userId)

                withContext(Dispatchers.Main) {
                    chatAdapter.addMessage(botMessage)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
     */

    /*
    // 채팅방 생성 (1st)
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
     */

    private suspend fun createDefaultChatRoom(userId: String): String {
        return try {
            // 현재 시간 기반 RoomName 생성
            val timestamp = System.currentTimeMillis()
            val roomName = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

            // Firestore에 저장할 채팅방 데이터
            val defaultRoom = mapOf(
                "participants" to listOf(userId, "bot"), // 유저와 봇 추가
                "createdAt" to timestamp,               // 생성 시간 추가
                "roomName" to roomName                  // RoomName 추가
            )

            // Firestore에 채팅방 추가
            val roomId = firestore.collection("chatrooms").add(defaultRoom).await().id

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ChatActivity, "새로운 채팅방이 생성되었습니다.", Toast.LENGTH_SHORT).show()
            }

            // 생성된 채팅방 ID 반환
            roomId
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ChatActivity, "채팅방 생성 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
            throw e
        }
    }


    // 메시지 전송
//    private fun sendMessage(message: String) {
//        val newMessage = ChatMessage("user", message, System.currentTimeMillis().toString())
//        chatAdapter.addMessage(newMessage) // 메시지 추가
//
//        // MongoDB에 메시지 저장 및 챗봇 응답 처리
//        CoroutineScope(Dispatchers.IO).launch {
//            currentRoomId?.let { roomId ->
//                try {
//                    // Firebase Firestore에 메시지 저장
//                    saveMessageToFirebase(roomId, newMessage)
//
//                    // 챗봇 응답 추가
//                    // val botReply = getBotReply(message)
//
//                    // Lite Interpreter 모델로 챗봇 응답 생성
//                    val botReply = chatModel.predict(message)
//                    val botMessage = ChatMessage("bot", botReply, System.currentTimeMillis().toString())
//                    saveMessageToFirebase(roomId, botMessage)
//
//                    withContext(Dispatchers.Main) {
//                        chatAdapter.addMessage(botMessage)
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }

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

    /*private suspend fun fetchChatRooms(roomId: String): List<ChatRoom> {
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
    }*/
    private suspend fun fetchChatRooms(userId: String): List<ChatRoom> {
        return try {
            db.collection("chatrooms")
                .whereArrayContains("participants", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING) // createdAt 기준으로 내림차순 정렬
                .get()
                .await()
                .documents.map { document ->
                    ChatRoom(
                        roomId = document.id,
                        participants = document["participants"] as List<String>,
                        roomName = document.getString("roomName") ?: "Unknown Room",
                        messages = fetchMessagesFromFirebase(document.id)
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchMessagesFromFirebase(roomId: String): List<ChatMessage> {
        return try {
            db.collection("chatrooms").document(roomId).collection("messages")
                .orderBy("timestamp") // timestamp 기준으로 오름차순 정렬
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

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            checkAndDeleteEmptyRoom(currentRoomId)
        }
    }

    private suspend fun checkAndDeleteEmptyRoom(roomId: String?) {
        if (roomId == null) return
        val messages = firestore.collection("chatrooms").document(roomId)
            .collection("messages").get().await()
        if (messages.isEmpty) {
            firestore.collection("chatrooms").document(roomId).delete().await()
        }
    }

    private fun saveMessageToFirebase(message: ChatMessage, userId: String) {
        val roomRef = firestore.collection("chatrooms").document(currentRoomId ?: return)
        roomRef.collection("messages").add(message)
    }

    private fun loadChatRoomMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            val messages = fetchMessagesFromFirebase(currentRoomId!!)
            withContext(Dispatchers.Main) {
                chatAdapter.updateMessage(messages)
            }
        }
    }

    private suspend fun getUserBasicInfo(userId: String): String {
        return try {
            val userDocument = firestore.collection("users").document(userId).get().await()
            if (userDocument.exists()) {
                val name = userDocument.getString("name") ?: "Unknown"
                val email = userDocument.getString("email") ?: "Unknown"
                val age = userDocument.getString("age") ?: "Unknown"
                val goal = userDocument.getString("goal") ?: "Unknown"
                val interest = userDocument.getString("interest") ?: "Unknown"
                val job = userDocument.getString("job") ?: "Unknown"

                // 기본 정보를 문자열로 조합
                "이름: $name, Email: $email, 나이: $age, 목표: $goal, 관심사: $interest, 직업: $job, "
            } else {
                "No additional user info available."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Failed to fetch user info."
        }
    }

}