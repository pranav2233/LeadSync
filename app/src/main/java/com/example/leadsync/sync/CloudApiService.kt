package com.example.leadsync.sync

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class CloudApiService {
    private val client = HttpClient(io.ktor.client.engine.okhttp.OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                },
            )
        }
    }

    suspend fun register(baseUrl: String, email: String, password: String): StoredSession {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
        val response = client.post("$normalizedBaseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(email = email.trim(), password = password))
        }.body<AuthResponse>()
        return StoredSession(
            baseUrl = normalizedBaseUrl,
            email = response.email,
            accessToken = response.accessToken,
        )
    }

    suspend fun login(baseUrl: String, email: String, password: String): StoredSession {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
        val response = client.post("$normalizedBaseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(email = email.trim(), password = password))
        }.body<AuthResponse>()
        return StoredSession(
            baseUrl = normalizedBaseUrl,
            email = response.email,
            accessToken = response.accessToken,
        )
    }

    suspend fun pushSnapshot(session: StoredSession, snapshot: CloudSnapshot) {
        client.put("${session.baseUrl}/sync/snapshot") {
            contentType(ContentType.Application.Json)
            bearerAuth(session.accessToken)
            setBody(SyncSnapshotRequest(snapshot))
        }
    }

    suspend fun pullSnapshot(session: StoredSession): CloudSnapshot? {
        return client.get("${session.baseUrl}/sync/snapshot") {
            bearerAuth(session.accessToken)
        }.body<SyncSnapshotResponse>().snapshot
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        return baseUrl.trim().removeSuffix("/")
    }
}
