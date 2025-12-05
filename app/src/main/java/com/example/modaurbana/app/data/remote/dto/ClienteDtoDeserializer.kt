package com.example.miappmodular.data.remote.dto.pedido

import com.example.modaurbana.app.data.remote.dto.User
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ClienteDtoDeserializer : JsonDeserializer<User> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): User {
        return when {
            // Caso 1: Es un String (solo ID)
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                User(
                    id = "",
                    email = "",
                    role = "CLIENTE",
                    isActive = true,
                    emailVerified = true,
                )
            }

            // Caso 2: Es un Object (cliente completo con populate)
            json.isJsonObject -> {
                val obj = json.asJsonObject
                User(
                    id = obj.get("_id")?.asString ?: "",
                    email = obj.get("email").asString,
                    role = obj.get("role").asString,
                    isActive = obj.get("isActive")?.asBoolean,
                    emailVerified = obj.get("emailVerified").asBoolean
                )
            }

            // Caso 3: Null o tipo inesperado
            else -> User(
                id = "",
                email = "",
                role = "",
                isActive = true,
                emailVerified = true,
            )
        }
    }
}
