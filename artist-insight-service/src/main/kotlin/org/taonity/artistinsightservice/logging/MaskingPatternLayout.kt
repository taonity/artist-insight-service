package org.taonity.artistinsightservice.logging

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.IntStream

class MaskingPatternLayout : PatternLayout() {
    private var multilinePattern: Pattern? = null
    private val maskPatterns: MutableList<String> = ArrayList()

    fun addMaskPattern(maskPattern: String) {
        maskPatterns.add(maskPattern)
        multilinePattern = Pattern.compile(maskPatterns.stream().collect(Collectors.joining("|")), Pattern.MULTILINE)
    }

    override fun doLayout(event: ILoggingEvent): String {
        return maskMessage(super.doLayout(event))
    }

    private fun maskMessage(message: String): String {
        if (multilinePattern == null) {
            return message
        }
        val sb = StringBuilder(message)
        val matcher: Matcher = multilinePattern!!.matcher(sb)
        while (matcher.find()) {
            IntStream.rangeClosed(1, matcher.groupCount()).forEach { group ->
                val matched = matcher.group(group)
                if (matched != null) {
                    val start = matcher.start(group)
                    val end = matcher.end(group)
                    maskSecretAtPosition(end, start, sb)
                }
            }
        }
        return sb.toString()
    }

    private fun maskSecretAtPosition(end: Int, start: Int, sb: StringBuilder) {
        val length = end - start
        if (length > 8) {
            // Mask all except first and last two characters
            for (i in start + 2 until end - 2) {
                sb.setCharAt(i, '*')
            }
        } else {
            // Mask all characters
            for (i in start until end) {
                sb.setCharAt(i, '*')
            }
        }
    }
}