package com.github.michaai.dontgetunderfootaiidea.settings

import com.intellij.openapi.components.SimplePersistentState
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.application.ApplicationManager

@State(
    name = "FimSettings",
    storages = [Storage("fim_settings.xml")]
)
class FimSettingsService : SimplePersistentState<FimSettingsService>() {
    var endpoint: String = "http://localhost:8080/v1/completions"
    var apiKey: String = ""
    var model: String = "deepseek-coder-6.7b"
    var maxTokens: Int = 256
    var temperature: Float = 0.3f
    var contextLines: Int = 10
    var enabled: Boolean = true
    var timeoutSeconds: Int = 30

    companion object {
        val instance: FimSettingsService
            get() = ApplicationManager.getApplication().getService(FimSettingsService::class.java)!!
    }
}
