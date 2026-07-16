package com.lamba.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lamba.app.network.RecommendationItem

class NotificationsAdapter : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {
    private val items = mutableListOf<RecommendationItem>()

    fun submitList(recommendations: List<RecommendationItem>) {
        items.clear()
        items.addAll(recommendations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_card, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNotificationDate: TextView = itemView.findViewById(R.id.tvNotificationDate)
        private val tvNotificationSeverity: TextView =
            itemView.findViewById(R.id.tvNotificationSeverity)
        private val tvNotificationTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvNotificationMessage: TextView =
            itemView.findViewById(R.id.tvNotificationMessage)

        fun bind(item: RecommendationItem) {
            tvNotificationDate.text = "\u0421\u0435\u0433\u043e\u0434\u043d\u044f"
            tvNotificationSeverity.text = if (item.severity == "warning") {
                "\u0412\u0430\u0436\u043d\u043e"
            } else {
                "\u0421\u043e\u0432\u0435\u0442"
            }
            tvNotificationTitle.text = item.title
            tvNotificationMessage.text = item.message
        }
    }
}
