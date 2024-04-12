package com.travudget.travudget

import android.content.Context
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
import android.content.SharedPreferences
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

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

                        val deutesString = jsonObject.optString("deutes")
                        val deutesMap = mutableMapOf<String, Int>()

                        if (deutesString.isNotEmpty()) {
                            val keyValuePairs = deutesString.replace("{", "").replace("}", "").split(", ")
                            for (pair in keyValuePairs) {
                                val keyValue = pair.split("=")
                                if (keyValue.size == 2) {
                                    val key = keyValue[0]
                                    val value = keyValue[1].toIntOrNull()
                                    if (value != null) {
                                        deutesMap[key] = value
                                    }
                                }
                            }
                        }

                        val participantsJsonArray = jsonObject.getJSONArray("emails_participants")
                        val participantsList = mutableListOf<String>()
                        for (i in 0 until participantsJsonArray.length()) {
                            val participantEmail = participantsJsonArray.getString(i)
                            participantsList.add(participantEmail)
                        }

                        viatge = ViatgeInfo(
                            viatgeId = viatgeId,
                            nomViatge = jsonObject.getString("nomViatge"),
                            dataInici = SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("dataInici")),
                            dataFi = if (jsonObject.isNull("dataFi")) null else SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("dataFi")),
                            divisa = jsonObject.getString("divisa"),
                            pressupostTotal = jsonObject.getInt("pressupostTotal"),
                            pressupostVariable = pressupostVariableMap,
                            deutes = deutesMap,
                            codi = jsonObject.getString("codi"),
                            creadorId = jsonObject.getInt("creador"),
                            participants = participantsList
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
    suspend fun getViatges(email: String?): List<ViatgeShowInfo> {
        var viatgesList = emptyList<ViatgeShowInfo>()
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
                        viatgesList = mutableListOf()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.getInt("id").toString()
                            val nomViatge = jsonObject.getString("nomViatge")
                            email?.let { ViatgeShowInfo(id, nomViatge, it) }
                                ?.let { (viatgesList as MutableList<ViatgeShowInfo>).add(it) }
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

            val requestBody = "{\"nomViatge\": \"${viatgeInfo.nomViatge}\", \"dataInici\": \"${sdfIniciStr}\", \"dataFi\": \"${sdfFiStr}\", \"divisa\": \"${viatgeInfo.divisa}\", \"pressupostTotal\": \"${viatgeInfo.pressupostTotal}\", \"deutes\": \"${viatgeInfo.deutes}\", \"pressupostVariable\": \"${viatgeInfo.pressupostVariable}\"}".toRequestBody(jsonMediaType)
            val url = "$backendUrl/usuaris/$email/viatges/$viatgeId"
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
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

    suspend fun deleteViatge(email: String?, viatgeId: String?) {
        try {
            val url = "$backendUrl/usuaris/$email/viatges/$viatgeId"
            val request = Request.Builder()
                .url(url)
                .delete()
                .build()
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("deleteViatge: OK")
                } else {
                    println("deleteViatge: Failed ${response.code}")
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

    suspend fun createDespesa(googleEmail: String?, despesaInfo: DespesaInfo) {
        try {
            val emailCreador = despesaInfo.emailCreador
            val idViatge = despesaInfo.viatgeId
            val viatgeInfo = getViatge(emailCreador, idViatge)

            if (viatgeInfo != null) {
                val deutors = despesaInfo.deutors
                if (deutors != null) {
                    if (deutors.isNotEmpty()) {
                        val nomDeute = deutors.keys.first()
                        val clau = "$nomDeute/$googleEmail"

                        if (viatgeInfo.deutes.containsKey(clau)) {
                            val value = viatgeInfo.deutes.getValue(clau)
                            val newValue = value + deutors.getValue(nomDeute)
                            viatgeInfo.deutes[clau] = newValue
                        } else {
                            viatgeInfo.deutes[clau] = deutors.getValue(nomDeute)
                        }
                        editViatge(emailCreador, viatgeInfo)
                    }
                }
            }

            val requestBody = "{\"nomDespesa\": \"${despesaInfo.nomDespesa}\", \"creador\": \"${emailCreador}\", \"descripcio\": \"${despesaInfo.descripcio}\", \"preu\": \"${despesaInfo.preu}\", \"categoria\": \"${despesaInfo.categoria}\", \"dataInici\": \"${despesaInfo.dataInici}\", \"dataFi\": \"${despesaInfo.dataFi}\", \"ubicacio_lat\": \"${despesaInfo.ubicacio_lat}\", \"ubicacio_long\": \"${despesaInfo.ubicacio_long}\", \"deutors\": \"${despesaInfo.deutors}\"}".toRequestBody(jsonMediaType)

            val url = "$backendUrl/usuaris/$emailCreador/viatges/$idViatge/despeses"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            print(request)
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                print(response)
                if (response.isSuccessful) {
                    println("createDespesa: OK")
                } else {
                    println("createDespesa: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun editDespesa(despesaInfo: DespesaInfo, despesaId: String) {
        try {
            val emailCreador = despesaInfo.emailCreador
            val lat = despesaInfo.ubicacio_lat?.toFloat()
            val long = despesaInfo.ubicacio_long?.toFloat()

            val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val dataIniciDate = originalFormat.parse(despesaInfo.dataInici)
            val dataFiDate = originalFormat.parse(despesaInfo.dataFi)

            val dataInici = targetFormat.format(dataIniciDate)
            val dataFi = targetFormat.format(dataFiDate)

            val requestBody = "{\"nomDespesa\": \"${despesaInfo.nomDespesa}\", \"creador\": \"${emailCreador}\", \"descripcio\": \"${despesaInfo.descripcio}\", \"preu\": \"${despesaInfo.preu}\", \"categoria\": \"${despesaInfo.categoria}\", \"dataInici\": \"${dataInici}\", \"dataFi\": \"${dataFi}\", \"ubicacio_lat\": \"${lat}\", \"ubicacio_long\": \"${long}\", \"deutors\": \"${despesaInfo.deutors}\"}".toRequestBody(jsonMediaType)
            val idViatge = despesaInfo.viatgeId
            val url = "$backendUrl/usuaris/$emailCreador/viatges/$idViatge/despeses/$despesaId"
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .build()
            print(request)
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                print(response)
                if (response.isSuccessful) {
                    println("editDespesa: OK")
                } else {
                    println("editDespesa: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun deleteDespesa(googleEmail: String, emailCreador: String, viatgeId: String, despesaId: String) {
        try {
            val viatgeInfo = getViatge(emailCreador, viatgeId)
            val despesaInfo = getDespesa(emailCreador, viatgeId, despesaId)

            if (viatgeInfo != null) {
                val deutors = despesaInfo?.deutors
                if (deutors != null && deutors.isNotEmpty()) {
                    val nomDeute = deutors.keys.first()
                    val clau = "$nomDeute/$googleEmail"

                    if (viatgeInfo.deutes.containsKey(clau)) {
                        val value = viatgeInfo.deutes.getValue(clau)
                        val newValue = value - deutors.getValue(nomDeute)
                        if (newValue <= 0) {
                            viatgeInfo.deutes.remove(clau)
                        } else {
                            viatgeInfo.deutes[clau] = newValue
                        }
                    }
                    editViatge(emailCreador, viatgeInfo)
                }
            }

            val url = "$backendUrl/usuaris/$emailCreador/viatges/$viatgeId/despeses/$despesaId"
            val request = Request.Builder()
                .url(url)
                .delete()
                .build()
            print(request)
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                print(response)
                if (response.isSuccessful) {
                    println("deleteDespesa: OK")
                } else {
                    println("deleteDespesa: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun getDespesa(email: String, viatgeId: String, despesaId: String): DespesaInfo? {
        var despesa: DespesaInfo? = null
        try {
            val url = "$backendUrl/usuaris/$email/viatges/$viatgeId/despeses/$despesaId"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("getDespesa: OK")
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        val jsonObject = JSONObject(json)
                        val deutorsString = jsonObject.optString("deutors")
                        val deutorsMap = mutableMapOf<String, Int>()

                        if (deutorsString.isNotEmpty()) {
                            val jsonObjectDeutors = JSONObject(deutorsString)
                            val keys = jsonObjectDeutors.keys()

                            keys.forEach { key ->
                                val value = jsonObjectDeutors.getInt(key)
                                deutorsMap[key] = value
                            }
                        }
                        despesa = DespesaInfo(
                            nomDespesa = jsonObject.getString("nomDespesa"),
                            viatgeId = jsonObject.getString("viatge"),
                            emailCreador = jsonObject.getString("creador"),
                            descripcio = jsonObject.optString("descripcio", null),
                            preu = jsonObject.getInt("preu"),
                            categoria = jsonObject.getString("categoria"),
                            dataInici = jsonObject.getString("dataInici"),
                            dataFi = if (jsonObject.isNull("dataFi")) null else jsonObject.getString("dataFi"),
                            ubicacio_lat = if (jsonObject.isNull("ubicacio_lat")) null else jsonObject.getDouble("ubicacio_lat"),
                            ubicacio_long = if (jsonObject.isNull("ubicacio_long")) null else jsonObject.getDouble("ubicacio_long"),
                            deutors = deutorsMap
                        )
                    }
                } else {
                    println("getDespesa: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return despesa
    }

    suspend fun getDespeses(emailCreador: String?, idViatge: String?): List<DespesaShowInfo> {
        var despesesList = emptyList<DespesaShowInfo>()
        try {
            val url = "$backendUrl/usuaris/$emailCreador/viatges/$idViatge/despeses"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            print(request)
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                print(response)
                if (response.isSuccessful) {
                    println("getDespeses: OK")
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        val jsonArray = JSONArray(json)
                        despesesList = mutableListOf()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val despesaId = jsonObject.getInt("id").toString()
                            val nomDespesa = jsonObject.getString("nomDespesa")
                            val dataInici = SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("dataInici"))
                            val preu = jsonObject.getInt("preu")
                            val categoria = jsonObject.getString("categoria")
                            val despesaInfo = DespesaShowInfo(despesaId, nomDespesa, dataInici, preu, categoria)
                            (despesesList as MutableList<DespesaShowInfo>).add(despesaInfo)
                        }
                        println("despesesList: $despesesList")
                    }
                } else {
                    println("getDespeses: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        println(despesesList)
        return despesesList
    }

    suspend fun getDespesesFiltrades(emailCreador: String?, idViatge: String?, categories: Array<String>?, preuMinim: Int, preuMaxim: Int): List<DespesaShowInfo> {
        var despesesList = emptyList<DespesaShowInfo>()
        try {
            val baseUrl = "$backendUrl/usuaris/$emailCreador/viatges/$idViatge/despeses"
            val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder()

            urlBuilder?.addQueryParameter("preuMinim", preuMinim.toString())
            urlBuilder?.addQueryParameter("preuMaxim", preuMaxim.toString())
            categories?.forEach { category ->
                urlBuilder?.addQueryParameter("categoria", category)
            }

            val url = urlBuilder?.build()
            println("URL: " + url)
            url?.let {
                val request = Request.Builder()
                    .url(it)
                    .get()
                    .build()

                withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let { json ->
                            val jsonArray = JSONArray(json)
                            despesesList = mutableListOf()
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val despesaId = jsonObject.getInt("id").toString()
                                val nomDespesa = jsonObject.getString("nomDespesa")
                                val dataInici = SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString("dataInici"))
                                val preu = jsonObject.getInt("preu")
                                val categoria = jsonObject.getString("categoria")
                                val despesaInfo = DespesaShowInfo(despesaId, nomDespesa, dataInici, preu, categoria)
                                (despesesList as MutableList<DespesaShowInfo>).add(despesaInfo)
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return despesesList
    }

    suspend fun getViatgesParticipant(email: String?): List<ViatgeShowInfo> {
        var viatgesList = emptyList<ViatgeShowInfo>()
        try {
            val url = "$backendUrl/usuaris/$email/viatges/share"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("getViatgesParticipant: OK")
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        val jsonArray = JSONArray(json)
                        viatgesList = mutableListOf()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val viatgeId = jsonObject.getInt("id").toString()
                            val nomViatge = jsonObject.getString("nomViatge")
                            val emailCreador = jsonObject.getString("creador_email")
                            val viatgeShowInfo = ViatgeShowInfo(viatgeId, nomViatge, emailCreador)
                            (viatgesList as MutableList<ViatgeShowInfo>).add(viatgeShowInfo)
                        }
                        println("viatgesList: $viatgesList")
                    }
                } else {
                    println("getViatgesParticipant: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        println(viatgesList)
        return viatgesList
    }

    suspend fun unirViatge(email: String?, codi: String): Boolean {
        try {
            val url = "$backendUrl/usuaris/$email/viatges/share"
            val requestBody = "{\"codi\": \"$codi\"}".toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("unirViatge: OK")
                    return@withContext true
                }
                else {
                    println("unirViatge: Failed ${response.code}")
                    return@withContext false
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return false
    }

    suspend fun expulsarViatge(email: String?, viatgeId: String) {
        try {
            val url = "$backendUrl/usuaris/$email/viatges/$viatgeId/share"
            val request = Request.Builder()
                .url(url)
                .delete()
                .build()
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("expulsarViatge: OK")
                }
                else {
                    println("expulsarViatge: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun getRecompensa(email: String?, recompensaId: String?) {
        try {
            val url = "$backendUrl/usuaris/$email/recompenses/$recompensaId"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            println(request)
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("getRecompensa: OK")

                } else {
                    println("getRecompensa: Failed ${response.code} ${response.message}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun createRecompensa(nomRecompensa: String, preu: Int, codi: String) {
        try {
            val requestBody = "{\"nomRecompensa\": \"$nomRecompensa\", \"preu\": \"$preu\", \"codi\": \"$codi\"}".toRequestBody(jsonMediaType)
            val url = "$backendUrl/recompenses"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("createRecompensa: OK")

                } else {
                    println("createRecompensa: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun getRecompenses(): List<RecompensaInfo> {
        var recompenses = emptyList<RecompensaInfo>()
        try {
            val url = "$backendUrl/recompenses"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("getRecompenses: OK")
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        val jsonArray = JSONArray(json)
                        recompenses = mutableListOf()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val recompensa = RecompensaInfo(
                                idRecompensa = jsonObject.getString("id"),
                                nomRecompensa = jsonObject.getString("nomRecompensa"),
                                codi = jsonObject.getString("codi"),
                                preu = jsonObject.getInt("preu")
                            )
                            (recompenses as MutableList<RecompensaInfo>).add(recompensa)
                        }
                    }
                } else {
                    println("getRecompenses: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return recompenses
    }

    suspend fun getPunts(email: String?): Int {
        var punts = 10000
        try {
            val url = "$backendUrl/usuaris/$email"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("getPunts: OK")
                    val responseBody = response.body?.string()
                    responseBody?.let { json ->
                        val jsonObject = JSONObject(json)
                        punts = jsonObject.getInt("punts")
                    }
                } else {
                    println("getPunts: Failed ${response.code}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return punts
    }
}
