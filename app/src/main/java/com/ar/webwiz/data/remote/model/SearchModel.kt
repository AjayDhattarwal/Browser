package com.ar.webwiz.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuggestionItem(
    val title: String ? = null,
    val status: Int? = null,
    val codes: List<Int>? = null,
    val details: Details? = null
)

@Serializable
data class Details(
    @SerialName("lm") val lm: List<String>? = null,
    @SerialName("zh") val title: String? = null,
    @SerialName("zi") val subtitle: String? = null,
    @SerialName("zp") val zp: Zp? = null,
    @SerialName("zs") val icon: String? = null,
    @SerialName("zf") val zf: Int? = null
)

@Serializable
data class Zp(
    val gs_ssp: String? = null,
)