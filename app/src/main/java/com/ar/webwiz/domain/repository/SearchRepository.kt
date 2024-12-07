package com.ar.webwiz.domain.repository

import com.ar.webwiz.data.remote.model.Details
import com.ar.webwiz.data.remote.model.SuggestionItem
import com.ar.webwiz.utils.function.extractString
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class SearchRepository(
    private val client: HttpClient
) {

    private  val BASE_URL_GOOGLE_SEARCH = "https://google.com/complete/search"

    suspend fun getSearchSuggestions(query: String): List<SuggestionItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response: String = client.get(BASE_URL_GOOGLE_SEARCH) {
                    url {
                        parameters.append("client", "gws-wiz")
                        parameters.append("q", query)
                    }
                }.body()
                val jsonString = response.extractString()

                println(jsonString)

                if (jsonString != null) {

                    val json = Json {
                        ignoreUnknownKeys = true
                    }

                    val jsonElement: JsonElement = json.parseToJsonElement(jsonString)

                    if (jsonElement is JsonArray) {
                        val firstElement = jsonElement[0].jsonArray

                        val suggestionItems = firstElement.map {
                            val itemArray = it.jsonArray
                            SuggestionItem(
                                title = itemArray.getOrNull(0)?.jsonPrimitive?.content,
                                status = itemArray.getOrNull(1)?.jsonPrimitive?.int,
                                codes = itemArray.getOrNull(2)?.jsonArray?.map { it.jsonPrimitive.int },
                                details = itemArray.getOrNull(3)?.let { detailElement ->
                                    json.decodeFromJsonElement<Details>(detailElement) // Use the same `json` instance here
                                }
                            )
                        }

                        return@withContext suggestionItems
                    }

                }
                emptyList()

            } catch (e: Exception) {
                println("Error: ${e}")
                emptyList()
            }
        }
    }


}