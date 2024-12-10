package com.example.financial_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private var messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == VIEW_TYPE_USER) {
            R.layout.right_chat_bubble
        } else {
            R.layout.left_chat_bubble
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view, viewType)
    }

    // 메시지 업데이트
    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender == "user") VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    fun addMessage(message: ChatMessage) {
        messages = messages + message
        notifyItemInserted(messages.size - 1)
    }

    class MessageViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = if (viewType == VIEW_TYPE_USER) {
            itemView.findViewById(R.id.user_message)
        } else {
            itemView.findViewById(R.id.bot_message)
        }

        fun bind(message: ChatMessage) {
            messageText.text = message.message
        }
    }

    companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_BOT = 1
    }
}

/*
// 1st (24.12.10 이전)
class ChatAdapter(private var messages: List<ChatMessage>) :

    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == VIEW_TYPE_USER) {
            R.layout.right_chat_bubble
        } else {
            R.layout.left_chat_bubble
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    // 메시지 업데이트
    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender == "user") VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    fun addMessage(message: ChatMessage) {
        messages = messages + message
        notifyItemInserted(messages.size - 1)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(
            if (itemViewType == VIEW_TYPE_USER) R.id.user_message else R.id.bot_message
        )

        fun bind(message: ChatMessage) {
            messageText.text = message.message
        }
    }

    companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_BOT = 1
    }
}

//class ChatAdapter(private var messages: List<ChatMessage>) :
//    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
//        val layout = if (viewType == 0) R.layout.right_chat_bubble else R.layout.left_chat_bubble
//        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
//        return MessageViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
//        holder.bind(messages[position])
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return if (messages[position].sender == "user") 0 else 1
//    }
//
//    override fun getItemCount(): Int = messages.size
//
//    fun addMessage(message: ChatMessage) {
//        messages = messages + message
//        notifyItemInserted(messages.size - 1)
//    }
//
//    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val messageText: TextView = itemView.findViewById(if (itemViewType == 0) R.id.user_message else R.id.bot_message)
//
//        fun bind(message: ChatMessage) {
//            messageText.text = message.message
//        }
//    }
//}
 */