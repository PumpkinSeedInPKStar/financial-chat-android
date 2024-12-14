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

    fun updateMessage(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    fun updateLastBotMessage(message: ChatMessage) {
        if (messages.isNotEmpty() && messages.last().sender == "bot") {
            messages = messages.dropLast(1) + message
            notifyItemChanged(messages.size - 1)
        } else {
            addMessage(message)
        }
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