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
}

data class ChatRequest(
    val model: String,
    val messages: List<ChatContent>
)

data class ChatContent(
    val role: String,
    val content: String
)

data class PartialChatResponse(
    val model: String,
    val message: ChatMessageResponse, // 일반 텍스트 응답 >> response
    val function: FunctionDetails?, // 함수 호출 정보
    val done: Boolean
)

data class ChatMessageResponse(
    val role: String,
    val content: String
)

data class FunctionDetails(
    val name: String,             // 호출할 함수 이름
    val arguments: String         // 함수에 전달할 JSON 형식의 인수
)