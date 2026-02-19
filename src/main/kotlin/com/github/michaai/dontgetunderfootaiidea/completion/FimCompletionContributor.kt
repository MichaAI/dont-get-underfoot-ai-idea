package com.github.michaai.dontgetunderfootaiidea.completion

import com.github.michaai.dontgetunderfootaiidea.settings.FimSettingsService
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class FimCompletionContributor : CompletionContributor() {
    private val log = logger<FimCompletionContributor>()
    private val fimClient = FimClient()

    override fun fillCompletionVariants(parameters: CompletionParameters, resultSet: CompletionResultSet) {
        if (!shouldProcess(parameters)) {
            return
        }

        val settings = FimSettingsService.instance
        if (!settings.enabled) {
            return
        }

        val editor = parameters.editor
        val file = parameters.originalFile

        if (isExcludedFile(file)) {
            return
        }

        val context = buildFimContext(editor, file, settings.contextLines)
        if (context.prefix.isEmpty() && context.suffix.isEmpty()) {
            return
        }

        val request = FimClient.FimRequest(
            prefix = context.prefix,
            suffix = context.suffix,
            model = settings.model,
            maxTokens = settings.maxTokens,
            temperature = settings.temperature,
            apiKey = settings.apiKey
        )

        val response = fimClient.complete(request)

        if (response.success && response.text.isNotBlank()) {
            val lookupElement = createLookupElement(response.text)
            resultSet.addElement(lookupElement)
        }
    }

    private fun shouldProcess(parameters: CompletionParameters): Boolean {
        return parameters.completionType == CompletionType.EXPLICIT
    }

    private fun isExcludedFile(file: PsiFile): Boolean {
        val excludedExtensions = listOf(".md", ".txt", ".json", ".xml", ".yaml", ".yml", ".toml", ".ini", ".properties")
        val name = file.name.lowercase()
        return excludedExtensions.any { name.endsWith(it) }
    }

    private fun buildFimContext(editor: Editor, file: PsiFile, contextLines: Int): FimContext {
        val document = editor.document
        val caretOffset = editor.caretModel.offset

        val prefixLines = mutableListOf<String>()
        var prefixLine = document.getLineNumber(caretOffset)
        val startLine = maxOf(0, prefixLine - contextLines)

        for (line in startLine until prefixLine) {
            val lineStart = document.getLineStartOffset(line)
            val lineEnd = document.getLineEndOffset(line)
            prefixLines.add(document.text.substring(lineStart, lineEnd))
        }

        if (caretOffset > 0 && caretOffset <= document.textLength) {
            val lineStart = document.getLineStartOffset(prefixLine)
            prefixLines.add(document.text.substring(lineStart, caretOffset))
        }

        val suffixLines = mutableListOf<String>()
        var suffixLine = document.getLineNumber(caretOffset)
        val endLine = minOf(document.lineCount - 1, suffixLine + contextLines)

        if (caretOffset < document.textLength) {
            val lineEnd = document.getLineEndOffset(suffixLine)
            suffixLines.add(document.text.substring(caretOffset, lineEnd))
        }

        for (line in (suffixLine + 1)..endLine) {
            val lineStart = document.getLineStartOffset(line)
            val lineEnd = document.getLineEndOffset(line)
            suffixLines.add(document.text.substring(lineStart, lineEnd))
        }

        return FimContext(
            prefix = prefixLines.joinToString("\n"),
            suffix = suffixLines.joinToString("\n")
        )
    }

    private fun createLookupElement(text: String): LookupElement {
        val trimmedText = text.trim()

        return LookupElementBuilder.create(trimmedText)
            .withInsertHandler(InsertHandler { context, item ->
                val insertedText = item.lookupString
                context.editor.caretModel.moveToOffset(context.tailOffset)
            })
    }

    data class FimContext(
        val prefix: String,
        val suffix: String
    )
}
