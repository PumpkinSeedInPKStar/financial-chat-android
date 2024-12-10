package com.example.financial_chat

import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApiService {
    @POST("api/chat")
    suspend fun generateChatCompletion(@Body request: ChatRequest): ChatResponse
}

data class ChatRequest(
    val model: String,
    val messages: List<ChatContent>
)

data class ChatContent(
    val role: String,
    val content: String
)

data class ChatResponse(
    val model: String,
    val message: ChatMessageResponse
)

data class ChatMessageResponse(
    val role: String,
    val content: String
)