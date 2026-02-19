package com.github.michaai.dontgetunderfootaiidea.settings

import com.intellij.ide.util.PropertiesComponent

class FimSettingsService {
    private val properties = PropertiesComponent.getInstance()

    var endpoint: String
        get() = properties.getValue("fim.endpoint", "http://localhost:8080/v1/completions")
        set(value) { properties.setValue("fim.endpoint", value) }

    var apiKey: String
        get() = properties.getValue("fim.apiKey", "")
        set(value) { properties.setValue("fim.apiKey", value) }

    var model: String
        get() = properties.getValue("fim.model", "deepseek-coder-6.7b")
        set(value) { properties.setValue("fim.model", value) }

    var maxTokens: Int
        get() = properties.getValue("fim.maxTokens", 256)
        set(value) { properties.setValue("fim.maxTokens", value) }

    var temperature: Float
        get() = properties.getValue("fim.temperature", 0.3f)
        set(value) { properties.setValue("fim.temperature", value) }

    var contextLines: Int
        get() = properties.getValue("fim.contextLines", 10)
        set(value) { properties.setValue("fim.contextLines", value) }

    var enabled: Boolean
        get() = properties.getValue("fim.enabled", true)
        set(value) { properties.setValue("fim.enabled", value) }

    var timeoutSeconds: Int
        get() = properties.getValue("fim.timeoutSeconds", 30)
        set(value) { properties.setValue("fim.timeoutSeconds", value) }

    companion object {
        val instance: FimSettingsService by lazy { FimSettingsService() }
    }
}

private fun <T> PropertiesComponent.getValue(key: String, default: T): T {
    return when (default) {
        is String -> getValue(key, default) as T
        is Int -> getInt(key, default) as T
        is Float -> getFloat(key, default) as T
        is Boolean -> getBoolean(key, default) as T
        else -> default
    }
}

private fun PropertiesComponent.setValue(key: String, value: Any) {
    when (value) {
        is String -> setValue(key, value)
        is Int -> setValue(key, value)
        is Float -> setValue(key, value)
        is Boolean -> setValue(key, value)
    }
}
