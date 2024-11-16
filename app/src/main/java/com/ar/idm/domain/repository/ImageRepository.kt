package com.ar.idm.domain.repository

import android.util.Base64
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageRepository(private val imageSearchClient: HttpClient) {


    private  val BASE_URL_GOOGLE_LENS = "https://lens.google.com/v3/upload?hl=en-IN&ep=gsbubb"

    suspend fun getImageSearchWebpage(file: File): String {
        return withContext(Dispatchers.IO){
            try {
                val response = uploadImage(file)
                if (response.status.isSuccess()) {
                    return@withContext response.bodyAsText()
                } else {
                    throw Exception("Error: ${response.status.value} - ${response.status.description}")
                }
            } catch (e: Exception) {
                throw Exception("Failed to upload image: ${e.message}", e)
            }
        }


    }



    suspend fun uploadImage(file: File): HttpResponse = withContext(Dispatchers.IO) {
        try {
            val response = imageSearchClient.submitFormWithBinaryData(
                url = BASE_URL_GOOGLE_LENS,
                formData = formData {
                    append("encoded_image", file.readBytes(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        append(HttpHeaders.ContentType, "image/jpeg")
                    })
                    append("processed_image_dimensions", "239,148")
                }
            )

            // Check for redirect response
            if (response.status.value in 300..399) {
                val newUrl = response.headers[HttpHeaders.Location]
                println("Redirecting to new URL: $newUrl") // Log the redirect URL
                if (newUrl != null) {
                    // Optionally, you can follow the redirect
                    return@withContext uploadImageToNewUrl(newUrl, file)
                }
            }

            return@withContext response
        } catch (e: Exception) {
            println("Error uploading image: ${e.message}")
            throw e // Rethrow the exception after logging
        }
    }

    private suspend fun uploadImageToNewUrl(newUrl: String, file: File): HttpResponse = withContext(Dispatchers.IO) {
        // Follow the redirect to the new URL
        return@withContext imageSearchClient.submitFormWithBinaryData(
            url = newUrl,
            formData = formData {
                append("encoded_image", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                    append(HttpHeaders.ContentType, "image/jpeg")
                })
                append("processed_image_dimensions", "239,148")
            }
        )
    }

}