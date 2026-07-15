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
            tvNotificationTitle.text = titleFor(item)
            tvNotificationMessage.text = messageFor(item)
        }

        private fun titleFor(item: RecommendationItem): String {
            return when (item.id) {
                "no_events" -> "\u0414\u043e\u0431\u0430\u0432\u044c\u0442\u0435 \u043f\u0435\u0440\u0432\u0443\u044e \u0437\u0430\u043f\u0438\u0441\u044c"
                "stale_records" -> "\u0414\u0430\u0432\u043d\u043e \u043d\u0435 \u043e\u0431\u043d\u043e\u0432\u043b\u044f\u043b\u0430\u0441\u044c \u0438\u0441\u0442\u043e\u0440\u0438\u044f \u0430\u0432\u0442\u043e\u043c\u043e\u0431\u0438\u043b\u044f"
                "high_fuel_price" -> "\u0412\u044b\u0441\u043e\u043a\u0438\u0435 \u0440\u0430\u0441\u0445\u043e\u0434\u044b \u043d\u0430 \u0442\u043e\u043f\u043b\u0438\u0432\u043e"
                "high_monthly_repair_cost" -> "\u0420\u0430\u0441\u0445\u043e\u0434\u044b \u043d\u0430 \u0440\u0435\u043c\u043e\u043d\u0442 \u0432\u044b\u0440\u043e\u0441\u043b\u0438"
                "recent_breakdown" -> "\u041f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u0430\u0432\u0442\u043e\u043c\u043e\u0431\u0438\u043b\u044c \u043f\u043e\u0441\u043b\u0435 \u043f\u043e\u043b\u043e\u043c\u043a\u0438"
                "long_distance_since_fuel" -> "\u041f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u0443\u0440\u043e\u0432\u0435\u043d\u044c \u0442\u043e\u043f\u043b\u0438\u0432\u0430"
                else -> item.title
            }
        }

        private fun messageFor(item: RecommendationItem): String {
            return when (item.id) {
                "no_events" -> "\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0435 \u0437\u0430\u043f\u0440\u0430\u0432\u043a\u0443, \u043f\u043e\u0435\u0437\u0434\u043a\u0443, \u0440\u0435\u043c\u043e\u043d\u0442 \u0438\u043b\u0438 \u043f\u0440\u043e\u0431\u043b\u0435\u043c\u0443, \u0447\u0442\u043e\u0431\u044b LAMBA \u043c\u043e\u0433\u043b\u0430 \u0434\u0430\u0432\u0430\u0442\u044c \u0442\u043e\u0447\u043d\u044b\u0435 \u0441\u043e\u0432\u0435\u0442\u044b."
                "stale_records" -> "\u0414\u043e\u0431\u0430\u0432\u044c\u0442\u0435 \u0441\u0432\u0435\u0436\u0438\u0435 \u0434\u0430\u043d\u043d\u044b\u0435 \u043e \u043f\u043e\u0435\u0437\u0434\u043a\u0430\u0445, \u0442\u043e\u043f\u043b\u0438\u0432\u0435 \u0438\u043b\u0438 \u0441\u0435\u0440\u0432\u0438\u0441\u0435, \u0447\u0442\u043e\u0431\u044b \u0441\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430 \u043e\u0441\u0442\u0430\u0432\u0430\u043b\u0430\u0441\u044c \u043f\u043e\u043b\u0435\u0437\u043d\u043e\u0439."
                "high_fuel_price" -> "\u041f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0445 \u0437\u0430\u043f\u0440\u0430\u0432\u043e\u043a \u0438 \u0441\u0440\u0430\u0432\u043d\u0438\u0442\u0435 \u0446\u0435\u043d\u044b \u043d\u0430 \u0430\u0437\u0441."
                "high_monthly_repair_cost" -> "\u041f\u043e\u0441\u043c\u043e\u0442\u0440\u0438\u0442\u0435, \u043d\u0435 \u043f\u043e\u0432\u0442\u043e\u0440\u044f\u044e\u0442\u0441\u044f \u043b\u0438 \u043e\u0434\u043d\u0438 \u0438 \u0442\u0435 \u0436\u0435 \u0440\u0430\u0431\u043e\u0442\u044b, \u0438 \u0437\u0430\u043f\u043b\u0430\u043d\u0438\u0440\u0443\u0439\u0442\u0435 \u0434\u0438\u0430\u0433\u043d\u043e\u0441\u0442\u0438\u043a\u0443."
                "recent_breakdown" -> "\u0415\u0441\u043b\u0438 \u043f\u0440\u043e\u0431\u043b\u0435\u043c\u0430 \u0435\u0449\u0435 \u043d\u0435 \u0443\u0441\u0442\u0440\u0430\u043d\u0435\u043d\u0430, \u043f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u043c\u0430\u0448\u0438\u043d\u0443 \u043f\u0435\u0440\u0435\u0434 \u0434\u0430\u043b\u044c\u043d\u0438\u043c\u0438 \u043f\u043e\u0435\u0437\u0434\u043a\u0430\u043c\u0438."
                "long_distance_since_fuel" -> "\u041f\u043e\u0441\u043b\u0435 \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u0435\u0439 \u0437\u0430\u043f\u0440\u0430\u0432\u043a\u0438 \u043f\u0440\u043e\u0439\u0434\u0435\u043d\u043e \u043c\u043d\u043e\u0433\u043e \u043a\u0438\u043b\u043e\u043c\u0435\u0442\u0440\u043e\u0432. \u041f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u0431\u0430\u043a \u043f\u0435\u0440\u0435\u0434 \u043f\u043e\u0435\u0437\u0434\u043a\u043e\u0439."
                else -> item.message
            }
        }
    }
}
