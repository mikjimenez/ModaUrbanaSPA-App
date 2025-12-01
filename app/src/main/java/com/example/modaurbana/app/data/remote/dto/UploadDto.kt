package com.example.modaurbana.app.data.remote.dto

data class UploadImageResponse(
    val success: Boolean,
    val message: String,
    val data: UploadImageData? = null
)

data class UploadImageData(
    val imagen: String,
    val imagenThumbnail: String
)