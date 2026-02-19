package com.github.michaai.dontgetunderfootaiidea.completion

import com.github.michaai.dontgetunderfootaiidea.settings.FimSettingsService
import com.intellij.openapi.diagnostic.logger
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class FimClient {
    private val log = logger<FimClient>()

    data class FimRequest(
        val prefix: String,
        val suffix: String,
        val model: String,
        val maxTokens: Int,
        val temperature: Float,
        val apiKey: String
    )

    data class FimResponse(
        val text: String,
        val success: Boolean,
        val error: String? = null
    )

    fun complete(request: FimRequest): FimResponse {
        val settings = FimSettingsService.instance
        val timeout = settings.timeoutSeconds * 1000

        return try {
            val url = URL(settings.endpoint)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.connectTimeout = timeout
            connection.readTimeout = timeout
            connection.setRequestProperty("Content-Type", "application/json")

            if (request.apiKey.isNotEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer ${request.apiKey}")
            }

            connection.doOutput = true

            val jsonBody = buildFimJson(request)
            val outputStream = connection.outputStream
            outputStream.write(jsonBody.toByteArray(StandardCharsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val completion = parseCompletionResponse(response)
                FimResponse(text = completion, success = true)
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                log.warn("FIM API error: $responseCode - $errorBody")
                FimResponse(text = "", success = false, error = "API error: $responseCode")
            }
        } catch (e: Exception) {
            log.warn("FIM request failed: ${e.message}")
            FimResponse(text = "", success = false, error = e.message)
        }
    }

    private fun buildFimJson(request: FimRequest): String {
        val settings = FimSettingsService.instance
        val fimPrompt = buildFimPrompt(request.prefix, request.suffix)

        return """
        {
            "model": "${request.model}",
            "prompt": $fimPrompt,
            "max_tokens": ${request.maxTokens},
            "temperature": ${request.temperature},
            "stop": ["<|endoftext|>", "<filename>", "</filename>", "<filepath>", "</<filepath>"]
        }
        """.trimIndent()
    }

    private fun buildFimPrompt(prefix: String, suffix: String): String {
        return "\"\"\"${escapeJson(prefix)}\"\"\""
    }

    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun parseCompletionResponse(response: String): String {
        return try {
            val regex = """"text"\s*:\s*"(.*?)",?""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(response)
            if (match != null) {
                val text = match.groupValues[1]
                text.replace("\\n", "\n").replace("\\\"", "\"")
            } else {
                ""
            }
        } catch (e: Exception) {
            log.warn("Failed to parse completion response: ${e.message}")
            ""
        }
    }
}
