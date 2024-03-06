package com.travudget.travudget

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackendManager {
    private val backendUrl = "http://127.0.0.1:8000"
    private val jsonMediaType = "application/json".toMediaType()
    private val client = OkHttpClient()

    suspend fun testFunction(number: Int) {
        try {
            val requestBody = "{\"number\": $number}".toRequestBody(jsonMediaType)
            println("Funciono")
            val request = Request.Builder()
                .url("http://127.0.0.1:8000")
                .post(requestBody)
                .build()
            println(request)
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("OK")
                } else {
                    println("Failed: ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
