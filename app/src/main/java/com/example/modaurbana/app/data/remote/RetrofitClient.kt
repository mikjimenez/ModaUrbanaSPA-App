package com.example.modaurbana.app.data.remote

import android.content.Context
import android.util.Log
import com.example.modaurbana.app.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TAG = "RetrofitClient"

    private const val BASE_URL = "https://modaurbana-api-sw96.onrender.com/api/"

    private lateinit var sessionManager: SessionManager

    private val okHttpClient: OkHttpClient by lazy {
        val authInterceptor = AuthInterceptor(sessionManager)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }
    private val retrofit: Retrofit? by lazy {
        try {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Retrofit init failed: ${e.message}", e)
            null
        }
    }

    // ApiService NO ES NULLABLE
    val ApiService: ApiService? by lazy {
        try {
            retrofit?.create(ApiService!!::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "ApiService init failed: ${e.message}", e)
            null
        }
    }
}