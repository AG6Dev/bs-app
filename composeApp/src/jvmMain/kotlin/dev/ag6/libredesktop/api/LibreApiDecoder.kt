package dev.ag6.libredesktop.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun <S> decodeLibreApiResponse(
    responseText: String,
    dataSerializer: KSerializer<@Serializable S>,
    json: Json
): LibreApiResponse<S> = json.decodeFromString(LibreApiResponseSerializer(dataSerializer), responseText)
