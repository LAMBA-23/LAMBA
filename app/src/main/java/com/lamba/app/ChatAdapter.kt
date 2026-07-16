package com.lamba.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Message(val text: String, val isFromUser: Boolean)

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_CAR = 2

        fun stripMarkdown(text: String): String {
            return text
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
                .replace(Regex("\\*(.+?)\\*"), "$1")
                .replace(Regex("__(.+?)__"), "$1")
                .replace(Regex("_(.+?)_"), "$1")
                .replace(Regex("~~(.+?)~~"), "$1")
                .replace(Regex("`(.+?)`"), "$1")
                .replace(Regex("```[\\s\\S]*?```"), "")
                .replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "")
                .replace(Regex("^[-*+]\\s+", RegexOption.MULTILINE), "")
                .replace(Regex("^\\d+\\.\\s+", RegexOption.MULTILINE), "")
                .trim()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromUser) TYPE_USER else TYPE_CAR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_USER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_car, parent, false)
            CarViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) holder.bind(message)
        else if (holder is CarViewHolder) holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txt: TextView = view.findViewById(R.id.tvMessageText)
        fun bind(m: Message) { txt.text = m.text }
    }

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txt: TextView = view.findViewById(R.id.tvMessageText)
        fun bind(m: Message) { txt.text = stripMarkdown(m.text) }
    }
}