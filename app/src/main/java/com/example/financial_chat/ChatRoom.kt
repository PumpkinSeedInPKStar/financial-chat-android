package com.example.financial_chat

data class ChatRoom(
    val roomId: String,
    val participants: List<String>, // ["user_id_1", "bot_id"]
    val messages: List<ChatMessage>
)
