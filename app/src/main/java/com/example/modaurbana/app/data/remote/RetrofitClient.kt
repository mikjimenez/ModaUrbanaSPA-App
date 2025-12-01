package com.example.modaurbana.app.data.remote

import android.content.Context
import com.example.modaurbana.app.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {


    private const val BASE_URL = "https://modaurbana-api-sw96.onrender.com/api"

    private lateinit var sessionManager: SessionManager

    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context.applicationContext
        sessionManager = SessionManager(context.applicationContext)
    }
    private val okHttpClient: OkHttpClient by lazy {
        val authInterceptor = AuthInterceptor(sessionManager)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val ApiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

}