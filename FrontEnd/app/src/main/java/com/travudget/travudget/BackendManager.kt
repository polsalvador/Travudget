package com.travudget.travudget

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BackendManager {
    private val backendUrl = "http://192.168.1.59:8000"
    private val jsonMediaType = "application/json".toMediaType()
    private val client = OkHttpClient()

    suspend fun sendLogin(name: String, email: String) {
        try {
            val requestBody = "{\"name\": \"$name\", \"email\": \"$email\"}".toRequestBody(jsonMediaType)
            val url = "$backendUrl/usuaris"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("Login: OK")
                } else {
                    println("Login: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
