package com.example.financial_chat

data class ChatRoom(
    val roomId: String,
    val participants: List<String>, // ["user_id_1", "bot_id"]
    val roomName: String, // 채팅방 이름 추가,
    val messages: List<ChatMessage>
)
