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
                    id = json.asString,
                    nombre = "",
                    email = "",
                    telefono = "",
                    direccion = null,
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
                    nombre = obj.get("nombre").asString,
                    email = obj.get("email").asString,
                    telefono = obj.get("telefono").asString,
                    direccion = obj.get("direccion")?.asString,
                    role = obj.get("role").asString,
                    isActive = obj.get("isActive")?.asBoolean,
                    emailVerified = obj.get("emailVerified").asBoolean
                )
            }

            // Caso 3: Null o tipo inesperado
            else -> User(
                id = "",
                nombre = "",
                email = "",
                telefono = "",
                direccion = null,
                role = "",
                isActive = true,
                emailVerified = true,
            )
        }
    }
}
