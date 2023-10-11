package org.yelog.ideavim.flash.action

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import org.yelog.ideavim.flash.KeyTagsGenerator
import org.yelog.ideavim.flash.MarksCanvas
import org.yelog.ideavim.flash.UserConfig
import kotlin.math.abs

private const val STATE_WAIT_SEARCH_CHAR = 0
private const val STATE_WAIT_KEY = 1

class Word1Finder : Finder {
    private var state = STATE_WAIT_SEARCH_CHAR
    private lateinit var s: String
    private lateinit var visibleRange: TextRange

    override fun start(e: Editor, visibleString: String, visibleRange: TextRange): List<MarksCanvas.Mark>? {
        this.s = visibleString
        this.visibleRange = visibleRange
        state = STATE_WAIT_SEARCH_CHAR
        return null
    }

    override fun input(e: Editor, c: Char, lastMarks: List<MarksCanvas.Mark>): List<MarksCanvas.Mark> {
        return when (state) {
            STATE_WAIT_SEARCH_CHAR -> {
                val cOffset = e.caretModel.offset
                var find = if (c.isLowerCase()) "(?i)" else ""
                find += "\\b" + Regex.escape("" + c)
                val offsets = Regex(find)
                    .findAll(s)
                    .map { it.range.first + visibleRange.startOffset }
                    .sortedBy { abs(cOffset - it) }
                    .toList()

                val tags =
                    KeyTagsGenerator.createTagsTree(offsets.size, UserConfig.getDataBean().characters)
                state = STATE_WAIT_KEY
                return offsets.zip(tags)
                    .map { MarksCanvas.Mark(it.second, it.first) }
                    .toList()
            }
            STATE_WAIT_KEY -> advanceMarks(c, lastMarks)
            else -> throw RuntimeException("Impossible.")
        }
    }
}
