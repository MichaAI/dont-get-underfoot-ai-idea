package com.github.michaai.dontgetunderfootaiidea.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing.*

class FimSettingsConfigurable : Configurable {
    private var panel: JPanel? = null
    private var endpointField: TextFieldWithBrowseButton? = null
    private var apiKeyField: JPasswordField? = null
    private var modelField: JTextField? = null
    private var maxTokensField: JSpinner? = null
    private var temperatureField: JSpinner? = null
    private var contextLinesField: JSpinner? = null
    private var enabledCheckBox: JCheckBox? = null
    private var timeoutField: JSpinner? = null

    override fun getDisplayName(): String = "AI FIM Completion"

    override fun createComponent(): JComponent {
        val settings = FimSettingsService.instance.state

        panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        enabledCheckBox = JCheckBox("Enable AI Completion").apply {
            isSelected = settings.enabled
            addActionListener {
                setFieldsEnabled(isSelected)
            }
        }
        panel!!.add(enabledCheckBox)

        panel!!.add(Box.createVerticalStrut(10))

        val endpointLabel = JLabel("API Endpoint:")
        endpointField = TextFieldWithBrowseButton().apply {
            text = settings.endpoint
        }
        panel!!.add(createRow(endpointLabel, endpointField!!))

        val apiKeyLabel = JLabel("API Key:")
        apiKeyField = JPasswordField(20).apply {
            text = settings.apiKey
            echoChar = '*'
        }
        panel!!.add(createRow(apiKeyLabel, apiKeyField!!))

        val modelLabel = JLabel("Model:")
        modelField = JTextField(20).apply {
            text = settings.model
        }
        panel!!.add(createRow(modelLabel, modelField!!))

        val maxTokensLabel = JLabel("Max Tokens:")
        maxTokensField = JSpinner(SpinnerNumberModel(settings.maxTokens, 32, 2048, 32))
        panel!!.add(createRow(maxTokensLabel, maxTokensField!!))

        val temperatureLabel = JLabel("Temperature:")
        temperatureField = JSpinner(SpinnerNumberModel(settings.temperature.toDouble(), 0.0, 2.0, 0.1))
        panel!!.add(createRow(temperatureLabel, temperatureField!!))

        val contextLinesLabel = JLabel("Context Lines:")
        contextLinesField = JSpinner(SpinnerNumberModel(settings.contextLines, 3, 50, 1))
        panel!!.add(createRow(contextLinesLabel, contextLinesField!!))

        val timeoutLabel = JLabel("Timeout (seconds):")
        timeoutField = JSpinner(SpinnerNumberModel(settings.timeoutSeconds, 5, 120, 5))
        panel!!.add(createRow(timeoutLabel, timeoutField!!))

        setFieldsEnabled(settings.enabled)

        return panel!!
    }

    private fun createRow(label: JLabel, component: JComponent): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(label)
            label.preferredSize = Dimension(120, label.preferredSize.height)
            add(Box.createHorizontalStrut(10))
            add(component)
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        endpointField?.isEnabled = enabled
        apiKeyField?.isEnabled = enabled
        modelField?.isEnabled = enabled
        maxTokensField?.isEnabled = enabled
        temperatureField?.isEnabled = enabled
        contextLinesField?.isEnabled = enabled
        timeoutField?.isEnabled = enabled
    }

    override fun isModified(): Boolean {
        val settings = FimSettingsService.instance.state
        return endpointField?.text != settings.endpoint ||
                String(apiKeyField?.password ?: charArrayOf()) != settings.apiKey ||
                modelField?.text != settings.model ||
                (maxTokensField?.value as? Int) != settings.maxTokens ||
                (temperatureField?.value as? Double)?.toFloat() != settings.temperature ||
                (contextLinesField?.value as? Int) != settings.contextLines ||
                enabledCheckBox?.isSelected != settings.enabled ||
                (timeoutField?.value as? Int) != settings.timeoutSeconds
    }

    override fun apply() {
        val settings = FimSettingsService.instance.state
        settings.endpoint = endpointField?.text ?: settings.endpoint
        settings.apiKey = String(apiKeyField?.password ?: charArrayOf())
        settings.model = modelField?.text ?: settings.model
        settings.maxTokens = maxTokensField?.value as? Int ?: settings.maxTokens
        settings.temperature = (temperatureField?.value as? Double)?.toFloat() ?: settings.temperature
        settings.contextLines = contextLinesField?.value as? Int ?: settings.contextLines
        settings.enabled = enabledCheckBox?.isSelected ?: settings.enabled
        settings.timeoutSeconds = timeoutField?.value as? Int ?: settings.timeoutSeconds
    }

    override fun reset() {
        val settings = FimSettingsService.instance.state
        endpointField?.text = settings.endpoint
        apiKeyField?.text = settings.apiKey
        modelField?.text = settings.model
        maxTokensField?.value = settings.maxTokens
        temperatureField?.value = settings.temperature.toDouble()
        contextLinesField?.value = settings.contextLines
        enabledCheckBox?.isSelected = settings.enabled
        timeoutField?.value = settings.timeoutSeconds
    }

    override fun disposeUIResources() {
        panel = null
    }
}
