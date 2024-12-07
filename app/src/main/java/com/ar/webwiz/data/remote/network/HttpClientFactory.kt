package com.ar.webwiz.data.remote.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json

object HttpClientFactory {

    fun createBasicClient(): HttpClient {
        return  HttpClient(CIO){
            followRedirects = true
            install(ContentNegotiation)
        }
    }

    fun createClientForGoogleLens(): HttpClient {
        return HttpClient(CIO) {
            followRedirects = true
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(DefaultRequest) {
                header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }
        }
    }

}