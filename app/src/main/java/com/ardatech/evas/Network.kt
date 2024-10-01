package com.ardatech.evas

import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

interface ApiInterface {
    suspend fun login(user: String, password: String): Response
    suspend fun logout(): Boolean
}

fun apiInterface(): ApiInterface = DummyBackend()

private class DummyBackend : ApiInterface {
    override suspend fun login(user: String, password: String): Response {
        delay(Random.nextInt(1500, 2500).milliseconds)

        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = "{\r\n    \"username\": \"$user\",\r\n    \"password\": \"$password\"       \r\n}"
            .toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://dummyjson.com/auth/login")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()
        val response = client.newCall(request).execute()
        return response
    }

    override suspend fun logout(): Boolean {
        delay(2000)
        return true
    }
}