package com.lamba.app.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface LambaApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("vehicle")
    suspend fun getVehicle(@Query("user_id") userId: Int): Response<Vehicle>

    @POST("vehicle")
    suspend fun createVehicle(@Body vehicle: VehicleRequest): Response<Vehicle>

    @POST("chat/parse-event")
    suspend fun parseChatEvent(@Body request: ChatParseRequest): Response<ChatParseResponse>

    @POST("chat/ask")
    suspend fun chatAsk(
        @Body request: ChatAskRequest,
        @Query("user_id") userId: Int,
    ): Response<ChatAskResponse>

    @POST("chat/title")
    suspend fun chatTitle(
        @Body request: ChatTitleRequest,
        @Query("user_id") userId: Int,
    ): Response<ChatTitleResponse>

    @GET("events")
    suspend fun getEvents(@Query("user_id") userId: Int? = null): Response<List<Event>>

    @POST("events")
    suspend fun createEvent(
        @Body event: EventCreateRequest,
        @Query("user_id") userId: Int,
    ): Response<Event>

    @POST("events")
    suspend fun createEventFromChat(
        @Body event: EventCreateRequest,
        @Query("user_id") userId: Int,
    ): Response<Event>

    @PUT("events/{event_id}")
    suspend fun updateEvent(
        @Path("event_id") eventId: Int,
        @Body event: EventCreateRequest,
        @Query("user_id") userId: Int,
    ): Response<Event>

    @DELETE("events/{event_id}")
    suspend fun deleteEvent(
        @Path("event_id") eventId: Int,
        @Query("user_id") userId: Int,
    ): Response<Unit>

    @GET("stats")
    suspend fun getStats(@Query("user_id") userId: Int? = null): Response<Stats>
}

object RetrofitClient {
    private const val BASE_URL = "http://186.246.27.211:8000/"

    val apiService: LambaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LambaApiService::class.java)
    }
}
