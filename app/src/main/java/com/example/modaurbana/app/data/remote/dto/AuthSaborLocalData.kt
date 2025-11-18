package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthSaborLocalData (
    val user: UserDto,

    @SerializedName("access_token")
    val accessToken: String,
)