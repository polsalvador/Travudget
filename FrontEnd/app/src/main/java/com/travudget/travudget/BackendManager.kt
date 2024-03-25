package com.travudget.travudget

import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.github.kittinunf.fuel.Fuel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import okhttp3.RequestBody
import okio.Buffer
class BackendManager {
    private val backendUrl = "http://192.168.1.59:8000"
    private val jsonMediaType = "application/json".toMediaType()
    private val client = OkHttpClient()
    private var exchangerate_key = "fce8e4ed89b7fcf440b2d9348923e1af"

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

    suspend fun getViatge(email: String?, viatgeId: String?): ViatgeInfo? {
        val viatgeId = viatgeId.toString()
        var viatge: ViatgeInfo? = null
        try {
            val url = "$backendUrl/usuaris/$email/viatges/$viatgeId"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        val jsonObject = JSONObject(json)
                        val pressupostVariableString = jsonObject.optString("pressupostVariable")
                        val pressupostVariableMap = mutableMapOf<String, Int>()

                        if (pressupostVariableString.isNotEmpty()) {
                            val keyValuePairs = pressupostVariableString.replace("{", "").replace("}", "").split(", ")
                            for (pair in keyValuePairs) {
                                val keyValue = pair.split("=")
                                if (keyValue.size == 2) {
                                    val key = keyValue[0]
                                    val value = keyValue[1].toIntOrNull()
                                    if (value != null) {
                                        pressupostVariableMap[key] = value
                                    }
                                }
                            }
                        }
                        viatge = ViatgeInfo(
                            viatgeId = viatgeId,
                            nomViatge = jsonObject.getString("nomViatge"),
                            dataInici = SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("dataInici")),
                            dataFi = if (jsonObject.isNull("dataFi")) null else SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("dataFi")),
                            divisa = jsonObject.getString("divisa"),
                            pressupostTotal = jsonObject.getInt("pressupostTotal"),
                            pressupostVariable = pressupostVariableMap,
                            deutes = JSONArray(jsonObject.getString("deutes")).toList(),
                            codi = jsonObject.getString("codi"),
                            creadorId = jsonObject.getInt("creador"),
                            participantsIds = JSONArray(jsonObject.getString("participants")).toList()
                        )
                    }
                } else {
                    println("getViatge: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return viatge
    }

    private fun JSONArray.toList(): List<Any> {
        val list = mutableListOf<Any>()
        for (i in 0 until length()) {
            val item = get(i)
            list.add(item)
        }
        return list
    }
    suspend fun getViatges(email: String?): List<Pair<Int, String>> {
        var viatgesList = emptyList<Pair<Int, String>>()
        try {
            val url = "$backendUrl/usuaris/$email/viatges"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            print(request)
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                print(response)
                if (response.isSuccessful) {
                    println("getViatges: OK")
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        val jsonArray = JSONArray(json)
                        viatgesList = mutableListOf<Pair<Int, String>>()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.getInt("id")
                            val nomViatge = jsonObject.getString("nomViatge")
                            (viatgesList as MutableList<Pair<Int, String>>).add(Pair(id, nomViatge))
                        }
                        println("viatgesList: $viatgesList")
                    }
                } else {
                    println("getViatges: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        println(viatgesList)
        return viatgesList
    }

    suspend fun createViatge(email: String?, nomViatge: String, dataInici: String, dataFi: String, divisa: String?): String {
        var viatgeId: String = ""
        try {
            val requestBody = "{\"nomViatge\": \"$nomViatge\", \"dataInici\": \"$dataInici\", \"dataFi\": \"$dataFi\", \"divisa\": \"$divisa\"}".toRequestBody(jsonMediaType)
            val url = "$backendUrl/usuaris/$email/viatges"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            print(request)
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                print(response)
                if (response.isSuccessful) {
                    println("createViatges: OK")
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        val jsonObject = JSONObject(json)
                        viatgeId = jsonObject.getInt("id").toString()
                    }
                } else {
                    println("createViatges: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return viatgeId
    }

    suspend fun editViatge(email: String?, viatgeInfo: ViatgeInfo) {
        try {
            val viatgeId = viatgeInfo.viatgeId

            val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val sdfIniciStr = sdfOutput.format(viatgeInfo.dataInici)
            val sdfFiStr = sdfOutput.format(viatgeInfo.dataFi)

            val requestBody = "{\"nomViatge\": \"${viatgeInfo.nomViatge}\", \"dataInici\": \"${sdfIniciStr}\", \"dataFi\": \"${sdfFiStr}\", \"divisa\": \"${viatgeInfo.divisa}\", \"pressupostTotal\": \"${viatgeInfo.pressupostTotal}\", \"pressupostVariable\": \"${viatgeInfo.pressupostVariable}\"}".toRequestBody(jsonMediaType)
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val requestBodyString = buffer.readUtf8()
            println("RequestBody: $requestBodyString")
            val url = "$backendUrl/usuaris/$email/viatges/$viatgeId"
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .build()
            print(request)
            print("viatgeId: $viatgeId")
            print("requestBody: $requestBody")
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                print(response)
                if (response.isSuccessful) {
                    println("editViatge: OK")
                } else {
                    println("editViatge: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun getCurrencies(): String {
        val baseUrl = "http://api.exchangeratesapi.io/v1/symbols"
        val apiKeyParam = "access_key=${URLEncoder.encode(exchangerate_key, "UTF-8")}"
        val baseCurrencyParam = "base=EUR"
        val urlString = "$baseUrl?$apiKeyParam&$baseCurrencyParam"

        return suspendCoroutine { continuation ->
            Fuel.get(urlString)
                .response { result ->
                    val (data, error) = result
                    if (error == null) {
                        val response = if (data != null) {
                            String(data)
                        } else {
                            ""
                        }
                        println("JSON: $response")
                        continuation.resume(response)
                    } else {
                        println("Error getCurrencies: $error")
                        continuation.resumeWithException(error.exception)
                    }
                }
        }
    }

    suspend fun changeCurrency(fromCurrency: String, toCurrency: String): String {
        val baseUrl = "http://api.exchangeratesapi.io/v1/symbols"
        val apiKeyParam = "access_key=${URLEncoder.encode(exchangerate_key, "UTF-8")}"
        val baseCurrencyParam = "base=EUR"
        val urlString = "$baseUrl?$apiKeyParam&$baseCurrencyParam"

        return suspendCoroutine { continuation ->
            Fuel.get(urlString)
                .response { result ->
                    val (data, error) = result
                    if (error == null) {
                        val response = if (data != null) {
                            String(data)
                        } else {
                            ""
                        }
                        println("JSON: $response")
                        continuation.resume(response)
                    } else {
                        println("Error getCurrencies: $error")
                        continuation.resumeWithException(error.exception)
                    }
                }
        }
    }
}
