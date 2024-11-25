package com.example.financial_chat

data class ChatMessage(
    val sender: String, // "user" or "bot"
    val message: String,
    val timestamp: String
)