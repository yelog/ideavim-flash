package com.github.yelog.ideavimflash.finder

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import org.yelog.ideavim.flash.KeyTagsGenerator
import org.yelog.ideavim.flash.MarksCanvas
import org.yelog.ideavim.flash.UserConfig
import kotlin.math.abs

private val pattern = Regex("(?i)\\b\\w")

class Word0Finder : Finder {
    override fun start(e: Editor, s: String, visibleRange: TextRange): List<MarksCanvas.Mark> {
        val cOffset = e.caretModel.offset
        val offsets = pattern.findAll(s)
            .map { it.range.first + visibleRange.startOffset }
            .sortedBy { abs(it - cOffset) }
            .toList()

        val tags = KeyTagsGenerator.createTagsTree(offsets.size, UserConfig.getDataBean().characters)

        return offsets.zip(tags)
            .map { MarksCanvas.Mark(it.second, it.first) }
            .toList()
    }

    override fun input(e: Editor, c: Char, lastMarks: List<MarksCanvas.Mark>): List<MarksCanvas.Mark> {
        return advanceMarks(c, lastMarks)
    }
}
