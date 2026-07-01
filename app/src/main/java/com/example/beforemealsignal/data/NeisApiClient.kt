package com.example.beforemealsignal.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

class HttpNeisApiClient(
  private val json: Json = Json { ignoreUnknownKeys = true },
) {
  suspend fun fetchWeeklyMeals(config: NeisConfig, weekRange: NeisWeekRange): List<NeisMealRow> {
    val responseBody =
      get(
        "https://open.neis.go.kr/hub/mealServiceDietInfo",
        weeklyMealParams(config, weekRange),
      )
    return parseMealRows(responseBody)
  }

  internal fun weeklyMealParams(config: NeisConfig, weekRange: NeisWeekRange): Map<String, String> =
    buildMap {
      put("KEY", config.apiKey)
      put("Type", "json")
      put("ATPT_OFCDC_SC_CODE", config.officeCode)
      put("SD_SCHUL_CODE", config.schoolCode)
      if (config.mealCode.isNotBlank()) put("MMEAL_SC_CODE", config.mealCode)
      put("MLSV_FROM_YMD", weekRange.startYmd)
      put("MLSV_TO_YMD", weekRange.endYmd)
    }

  private fun get(baseUrl: String, params: Map<String, String>): String {
    val query =
      params.entries.joinToString("&") { (key, value) ->
        "${key.urlEncode()}=${value.urlEncode()}"
      }
    val connection = URL("$baseUrl?$query").openConnection() as HttpURLConnection
    return try {
      connection.requestMethod = "GET"
      connection.connectTimeout = 10_000
      connection.readTimeout = 10_000
      connection.useCaches = false

      val status = connection.responseCode
      val stream = if (status in 200..299) connection.inputStream else connection.errorStream
      val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
      if (status !in 200..299) {
        throw IOException("NEIS request failed with HTTP $status")
      }
      body
    } finally {
      connection.disconnect()
    }
  }

  fun parseMealRows(responseBody: String): List<NeisMealRow> {
    val root = runCatching { json.parseToJsonElement(responseBody).jsonObject }.getOrNull() ?: return emptyList()
    val sections = root["mealServiceDietInfo"] as? JsonArray ?: return emptyList()
    val rows = sections.firstNotNullOfOrNull { section -> section.jsonObjectOrNull()?.get("row") as? JsonArray } ?: return emptyList()

    return rows.mapNotNull { element ->
      val row = element.jsonObjectOrNull() ?: return@mapNotNull null
      val servedDate = row.string("MLSV_YMD")
      val dishName = row.string("DDISH_NM")
      if (servedDate.isBlank() || dishName.isBlank()) return@mapNotNull null
      NeisMealRow(
        schoolName = row.string("SCHUL_NM"),
        mealName = row.string("MMEAL_SC_NM").ifBlank { "중식" },
        servedDate = servedDate,
        dishName = dishName,
      )
    }
  }

  private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")

  private fun kotlinx.serialization.json.JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

  private fun JsonObject.string(name: String): String = (get(name) as? JsonPrimitive)?.contentOrNull.orEmpty()
}
