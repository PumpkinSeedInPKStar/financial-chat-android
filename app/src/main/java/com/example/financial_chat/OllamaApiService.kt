package com.example.financial_chat

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OllamaApiService {
    @Streaming
    @POST("api/chat")
    suspend fun generateChatCompletion(@Body request: ChatRequest): ResponseBody
//    suspend fun generateChatCompletion(@Body request: ChatRequest): ChatResponse
}

data class ChatRequest(
    val model: String,
    val messages: List<ChatContent>
)

data class ChatContent(
    val role: String,
    val content: String
)

/*
data class ChatResponse(
    @SerializedName("model") val model: String,
    @SerializedName("output") val message: ChatMessageResponse
//    val model: String,
//    val message: ChatMessageResponse
)

data class ChatMessageResponse(
    val role: String,
    val content: String
)
*/

data class PartialChatResponse(
    val model: String,
    val message: ChatMessageResponse,
    val done: Boolean
)

data class ChatMessageResponse(
    val role: String,
    val content: String
)