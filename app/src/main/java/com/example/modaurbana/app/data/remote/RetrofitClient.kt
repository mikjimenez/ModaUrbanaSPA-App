package com.example.modaurbana.app.data.remote

import android.content.Context
import android.util.Log
import com.example.miappmodular.data.remote.dto.pedido.ClienteDtoDeserializer
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.remote.dto.User
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder

object RetrofitClient {

    private const val BASE_URL = "https://modaurbana-api-sw96.onrender.com/api/"

    /**
     * TokenManager para gestión segura de tokens JWT.
     * Se inicializa llamando a initialize() con el contexto de la app.
     */
    private lateinit var tokenManager: SessionManager

    /**
     * Inicializa el RetrofitClient con el contexto de la aplicación.
     *
     * **IMPORTANTE:** Debe llamarse UNA SOLA VEZ al inicio de la app,
     * en MainActivity.onCreate() o en Application.onCreate().
     *
     * @param context Contexto de la aplicación (preferiblemente ApplicationContext)
     *
     * Ejemplo:
     * ```kotlin
     * class MainActivity : ComponentActivity() {
     *     override fun onCreate(savedInstanceState: Bundle?) {
     *         super.onCreate(savedInstanceState)
     *         RetrofitClient.initialize(this)
     *     }
     * }
     * ```
     */
    fun initialize(context: Context) {
        tokenManager = SessionManager(context.applicationContext)
    }

    /**
     * Cliente HTTP OkHttp configurado con interceptores y timeouts.
     *
     * **Configuración:**
     * 1. **AuthInterceptor:** Añade automáticamente el header Authorization con el JWT token
     * 2. **HttpLoggingInterceptor:** Registra todas las peticiones y respuestas (útil para debugging)
     * 3. **Timeouts aumentados para Render.com free tier:**
     *    - connectTimeout: 60s (para cold starts)
     *    - readTimeout: 90s (para operaciones lentas)
     *    - writeTimeout: 60s (para uploads grandes)
     *
     * **Lazy initialization:**
     * Se crea solo cuando se accede por primera vez. Thread-safe por defecto en Kotlin.
     */
    private val okHttpClient: OkHttpClient by lazy {
        // AuthInterceptor añade el token JWT a las peticiones automáticamente
        val authInterceptor = AuthInterceptor(tokenManager)

        // LoggingInterceptor registra las peticiones y respuestas para debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)  // Aumentado para cold starts de Render
            .readTimeout(90, TimeUnit.SECONDS)     // Aumentado para operaciones lentas
            .writeTimeout(60, TimeUnit.SECONDS)    // Aumentado para uploads grandes
            .build()
    }

    /**
     * Configuración personalizada de Gson con deserializadores custom.
     *
     * **ClienteDtoDeserializer:**
     * Maneja el caso donde el backend retorna `cliente` como String en listas
     * y como Object en detalles (debido a .populate() de MongoDB).
     */
    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(User::class.java, ClienteDtoDeserializer())
            .create()
    }

    /**
     * Instancia singleton de Retrofit.
     *
     * Retrofit convierte interfaces Kotlin en clientes HTTP funcionales,
     * manejando automáticamente:
     * - Serialización JSON ↔ Objetos Kotlin (con Gson)
     * - Manejo de URLs, headers y parámetros
     * - Integración con corrutinas (suspend functions)
     *
     * **Lazy initialization:**
     * Se crea solo cuando se necesita por primera vez.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ========= API Services - Organizados por dominio =========

    /**
     * API service para autenticación (login, registro, perfil)
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun getTokenManager(): SessionManager = tokenManager
}