package com.example.modaurbana.app.data.remote

import com.example.modaurbana.app.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTENTICACIÓN (AuthDto, UserDto) ====================

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("auth/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @GET("auth/users")
    suspend fun getAllUsers(): Response<UsersResponse>

    // ==================== PERFIL DE CLIENTE (ClienteProfileDto) ====================

    @GET("cliente-profile/me")
    suspend fun getMyProfile(): Response<ClienteProfileResponse>

    @PUT("cliente-profile/me")
    suspend fun updateMyProfile(
        @Body request: UpdateClienteProfileRequest
    ): Response<ClienteProfileResponse>

    @GET("cliente-profile")
    suspend fun getAllClienteProfiles(): Response<ClienteProfilesResponse>

    @GET("cliente-profile/{userId}")
    suspend fun getClienteProfileByUserId(
        @Path("userId") userId: String
    ): Response<ClienteProfileResponse>

    // ==================== CATEGORÍAS (CategoriaDto, UploadDto) ====================

    @GET("categoria")
    suspend fun getCategorias(): Response<CategoriasResponse>

    @GET("categoria/{id}")
    suspend fun getCategoriaById(
        @Path("id") id: String
    ): Response<CategoriaResponse>

    @POST("categoria")
    suspend fun createCategoria(
        @Body request: CreateCategoriaRequest
    ): Response<CategoriaResponse>

    @PATCH("categoria/{id}")
    suspend fun updateCategoria(
        @Path("id") id: String,
        @Body request: UpdateCategoriaRequest
    ): Response<CategoriaResponse>

    @DELETE("categoria/{id}")
    suspend fun deleteCategoria(
        @Path("id") id: String
    ): Response<MessageResponse>

    @Multipart
    @POST("categoria/{id}/upload-image")
    suspend fun uploadCategoriaImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ==================== PRODUCTOS (UploadDto, CommonResponse, ProductoDto)====================

    @GET("producto")
    suspend fun getProductos(): Response<ProductosResponse>

    @GET("producto/{id}")
    suspend fun getProductoById(
        @Path("id") id: String
    ): Response<ProductoResponse>

    @GET("producto/categoria/{categoriaId}")
    suspend fun getProductosByCategoria(
        @Path("categoriaId") categoriaId: String
    ): Response<ProductosResponse>

    @GET("producto/filtros")
    suspend fun filtrarProductos(
        @Query("talla") talla: String? = null,
        @Query("material") material: String? = null,
        @Query("estilo") estilo: String? = null,
        @Query("genero") genero: String? = null,
        @Query("precioMin") precioMin: Double? = null,
        @Query("precioMax") precioMax: Double? = null
    ): Response<ProductosResponse>

    @GET("producto/tendencias")
    suspend fun getProductosTendencias(): Response<ProductosResponse>

    @POST("producto")
    suspend fun createProducto(
        @Body request: CreateProductoRequest
    ): Response<ProductoResponse>

    @PATCH("producto/{id}")
    suspend fun updateProducto(
        @Path("id") id: String,
        @Body request: UpdateProductoRequest
    ): Response<ProductoResponse>

    @DELETE("producto/{id}")
    suspend fun deleteProducto(
        @Path("id") id: String
    ): Response<MessageResponse>

    @Multipart
    @POST("producto/{id}/upload-image")
    suspend fun uploadProductoImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ==================== CARRITO (CommonResponse, CarritoDto, PedidoDto) ====================

    @GET("carrito")
    suspend fun getCarritos(): Response<CarritosResponse>

    @GET("carrito/{id}")
    suspend fun getCarritoById(
        @Path("id") id: String
    ): Response<CarritoResponse>

    @GET("carrito/cliente/{clienteId}")
    suspend fun getCarritoByCliente(
        @Path("clienteId") clienteId: String
    ): Response<CarritoResponse>

    @POST("carrito")
    suspend fun createCarrito(
        @Body request: CreateCarritoRequest
    ): Response<CarritoResponse>

    @POST("carrito/agregar-item")
    suspend fun agregarItemCarrito(
        @Body request: AgregarItemCarritoRequest
    ): Response<CarritoResponse>

    @DELETE("carrito/cliente/{clienteId}/item/{itemId}")
    suspend fun removerItemCarrito(
        @Path("clienteId") clienteId: String,
        @Path("itemId") itemId: String
    ): Response<MessageResponse>

    @POST("carrito/confirmar-pedido")
    suspend fun confirmarPedido(
        @Body request: ConfirmarPedidoRequest
    ): Response<PedidoResponse>

    @PATCH("carrito/{id}")
    suspend fun updateCarrito(
        @Path("id") id: String,
        @Body request: UpdateCarritoRequest
    ): Response<CarritoResponse>

    @DELETE("carrito/{id}")
    suspend fun deleteCarrito(
        @Path("id") id: String
    ): Response<MessageResponse>

    // ==================== PEDIDOS (CommonResponse, UploadDto, PedidoDto)====================

    @GET("pedido")
    suspend fun getPedidos(): Response<PedidosResponse>

    @GET("pedido/{id}")
    suspend fun getPedidoById(
        @Path("id") id: String
    ): Response<PedidoResponse>

    @POST("pedido")
    suspend fun createPedido(
        @Body request: CreatePedidoRequest
    ): Response<PedidoResponse>

    @PATCH("pedido/{id}")
    suspend fun updatePedido(
        @Path("id") id: String,
        @Body request: UpdatePedidoRequest
    ): Response<PedidoResponse>

    @DELETE("pedido/{id}")
    suspend fun deletePedido(
        @Path("id") id: String
    ): Response<MessageResponse>

    @Multipart
    @POST("pedido/{id}/upload-image")
    suspend fun uploadPedidoImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ==================== CLIENTES (CommonResponse, UploadDto, ClienteDto)====================

    @GET("cliente")
    suspend fun getClientes(): Response<ClientesResponse>

    @GET("cliente/{id}")
    suspend fun getClienteById(
        @Path("id") id: String
    ): Response<ClienteResponse>

    @POST("cliente")
    suspend fun createCliente(
        @Body request: CreateClienteRequest
    ): Response<ClienteResponse>

    @PATCH("cliente/{id}")
    suspend fun updateCliente(
        @Path("id") id: String,
        @Body request: UpdateClienteRequest
    ): Response<ClienteResponse>

    @DELETE("cliente/{id}")
    suspend fun deleteCliente(
        @Path("id") id: String
    ): Response<MessageResponse>

    @Multipart
    @POST("cliente/{id}/upload-image")
    suspend fun uploadClienteImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ==================== UPLOAD (UploadDto)====================

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ==================== HEALTH CHECK ====================

    @GET("health")
    suspend fun healthCheck(): Response<HealthCheckResponse>
}



