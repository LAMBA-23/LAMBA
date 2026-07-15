package com.lamba.app

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lamba.app.network.RecommendationItem
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.SessionManager
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {
    private lateinit var progressNotifications: View
    private lateinit var rvNotifications: RecyclerView
    private lateinit var tvNotificationsState: TextView
    private val adapter = NotificationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<ImageButton>(R.id.btnNotificationsBack).setOnClickListener {
            finish()
        }
        progressNotifications = findViewById(R.id.progressNotifications)
        rvNotifications = findViewById(R.id.rvNotifications)
        tvNotificationsState = findViewById(R.id.tvNotificationsState)

        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = SessionManager.getUserId(this)
        if (userId == null) {
            showState("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0438\u0442\u044c \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044f.")
            return
        }

        showLoading()
        lifecycleScope.launch {
            runCatching { RetrofitClient.apiService.getRecommendations(userId) }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        val recommendations = response.body()?.recommendations.orEmpty()
                        if (recommendations.isEmpty()) {
                            showState("\u041f\u043e\u043a\u0430 \u043d\u0435\u0442 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439")
                        } else {
                            showNotifications(recommendations)
                            SessionManager.markRecommendationsViewed(
                                this@NotificationsActivity,
                                userId,
                                recommendations,
                            )
                        }
                    } else {
                        showState("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f.")
                    }
                }
                .onFailure {
                    showState("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f.")
                }
        }
    }

    private fun showLoading() {
        progressNotifications.visibility = View.VISIBLE
        rvNotifications.visibility = View.GONE
        tvNotificationsState.visibility = View.GONE
    }

    private fun showNotifications(recommendations: List<RecommendationItem>) {
        progressNotifications.visibility = View.GONE
        tvNotificationsState.visibility = View.GONE
        rvNotifications.visibility = View.VISIBLE
        adapter.submitList(recommendations)
    }

    private fun showState(message: String) {
        progressNotifications.visibility = View.GONE
        rvNotifications.visibility = View.GONE
        tvNotificationsState.text = message
        tvNotificationsState.visibility = View.VISIBLE
    }
}
