package com.lamba.app.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LambaApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("chat/parse-event")
    suspend fun parseChatEvent(@Body request: ChatParseRequest): Response<ChatParseResponse>

    @GET("events")
    suspend fun getEvents(): Response<List<Event>>

    @POST("events")
    suspend fun createEvent(@Body event: Event): Response<Event>

    @GET("stats")
    suspend fun getStats(): Response<Stats>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.93.26.193:8000/"

    val apiService: LambaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LambaApiService::class.java)
    }
}
