package com.github.strindberg.emacsj.search

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColors
import java.lang.Character.isUpperCase
import java.lang.Character.toLowerCase
import java.lang.Character.toUpperCase
import com.intellij.openapi.editor.colors.TextAttributesKey

enum class SearchType { TEXT, REGEXP }

//internal val EMACSJ_PRIMARY = TextAttributesKey.createTextAttributesKey("EMACSJ_PRIMARY")
internal val EMACSJ_PRIMARY = TextAttributesKey.createTextAttributesKey("EMACSJ_PRIMARY", EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES)

//internal val EMACSJ_SECONDARY = TextAttributesKey.createTextAttributesKey("EMACSJ_SECONDARY")
internal val EMACSJ_SECONDARY = TextAttributesKey.createTextAttributesKey("EMACSJ_SECONDARY", EditorColors.WRITE_SEARCH_RESULT_ATTRIBUTES)

internal fun caseSensitive(text: String): Boolean = text.any { isUpperCase(it) && toUpperCase(it) != toLowerCase(it) }

internal fun <T> prependElement(element: T, list: List<T>) =
    (listOf(element) + list).distinct().take(64) // Remove duplicates, keep most recent

internal fun nextPos(oldPos: Int) = maxOf(oldPos - 1, -1)

internal fun previousPos(oldPos: Int, list: List<*>) = minOf(oldPos + 1, list.lastIndex)
