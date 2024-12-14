package com.example.financial_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatRoomAdapter (
    private var chatRooms: MutableList<ChatRoom>,
    private val onClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatRoomName: TextView = itemView.findViewById(R.id.chat_room_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_room_item, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.chatRoomName.text = chatRoom.roomName // 채팅방 이름
        holder.itemView.setOnClickListener {
            onClick(chatRoom)
        }
    }

    override fun getItemCount(): Int = chatRooms.size

//    fun submitList(newChatRooms: List<ChatRoom>) {
//        chatRooms = newChatRooms
//        notifyDataSetChanged()
//    }

    // ChatRooms 업데이트 함수
    fun updateChatRooms(newChatRooms: List<ChatRoom>) {
        chatRooms.clear()
        chatRooms.addAll(newChatRooms)
        notifyDataSetChanged()
    }

    fun addChatRoom(chatRoom: ChatRoom) {
        chatRooms.add(chatRoom)
        notifyItemInserted(chatRooms.size - 1)
    }

}