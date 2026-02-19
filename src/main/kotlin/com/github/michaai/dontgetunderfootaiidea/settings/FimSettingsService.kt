package com.github.michaai.dontgetunderfootaiidea.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.SimpleState
import com.intellij.serialization.annotations.Property

class FimSettingsState {
    @Property
    var endpoint: String = "http://localhost:8080/v1/completions"

    @Property
    var apiKey: String = ""

    @Property
    var model: String = "deepseek-coder-6.7b"

    @Property
    var maxTokens: Int = 256

    @Property
    var temperature: Float = 0.3f

    @Property
    var contextLines: Int = 10

    @Property
    var enabled: Boolean = true

    @Property
    var timeoutSeconds: Int = 30
}

class FimSettingsService : SimpleState<FimSettingsState>() {
    init {
        initState(FimSettingsState())
    }

    fun getState(): FimSettingsState = state

    companion object {
        val instance: FimSettingsService
            get() = ApplicationManager.getApplication().getService(FimSettingsService::class.java)!!
    }
}
