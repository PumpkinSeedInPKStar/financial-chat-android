package com.example.financial_chat
import androidx.appcompat.app.AppCompatActivity
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document

class ChatActivity : AppCompatActivity(){

}

// 채팅방 생성 코드
fun createChatRoom(chatRoomName: String): Boolean {
    val client = MongoClients.create("your_mongodb_connection_string")
    val database = client.getDatabase("chatApp")
    val collection: MongoCollection<Document> = database.getCollection("chatrooms")

    val newChatRoom = Document("name", chatRoomName)
        .append("created_at", System.currentTimeMillis())
        .append("messages", emptyList<Document>()) // 초기 메시지는 비어 있음

    collection.insertOne(newChatRoom)
    client.close()
    return true
}

// 메시지 저장 코드
fun addMessageToChatRoom(chatRoomId: String, sender: String, content: String) {
    val client = MongoClients.create("your_mongodb_connection_string")
    val database = client.getDatabase("chatApp")
    val collection: MongoCollection<Document> = database.getCollection("chatrooms")

    val message = Document("sender", sender)
        .append("content", content)
        .append("timestamp", System.currentTimeMillis())

    collection.updateOne(
        Document("_id", chatRoomId),
        Document("\$push", Document("messages", message))
    )
    client.close()
}

// 채팅방 목록 가져오기
fun getChatRooms(): List<Document> {
    val client = MongoClients.create("your_mongodb_connection_string")
    val database = client.getDatabase("chatApp")
    val collection: MongoCollection<Document> = database.getCollection("chatrooms")

    val chatRooms = collection.find().toList()
    client.close()
    return chatRooms
}

// 특정 채팅방 메시지 가져오기
fun getMessagesFromChatRoom(chatRoomId: String): List<Document> {
    val client = MongoClients.create("your_mongodb_connection_string")
    val database = client.getDatabase("chatApp")
    val collection: MongoCollection<Document> = database.getCollection("chatrooms")

    val chatRoom = collection.find(Document("_id", chatRoomId)).first()
    client.close()
    return chatRoom?.getList("messages", Document::class.java) ?: emptyList()
}

