package org.taonity.artistinsightservice.infrastructure.logging

import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.IntStream

class LogMasker {

    private var multilinePattern: Pattern? = null
    private val maskPatterns: MutableList<String> = ArrayList()

    fun addMaskPattern(maskPattern: String) {
        maskPatterns.add(maskPattern)
        multilinePattern = Pattern.compile(
            maskPatterns.stream().collect(Collectors.joining("|")),
            Pattern.MULTILINE
        )
    }

    fun mask(message: String): String {
        if (multilinePattern == null) {
            return message
        }
        val sb = StringBuilder(message)
        val matcher = multilinePattern!!.matcher(sb)
        while (matcher.find()) {
            IntStream.rangeClosed(1, matcher.groupCount()).forEach { group ->
                val matched = matcher.group(group)
                if (matched != null) {
                    maskSecretAtPosition(matcher.start(group), matcher.end(group), sb)
                }
            }
        }
        return sb.toString()
    }

    private fun maskSecretAtPosition(start: Int, end: Int, sb: StringBuilder) {
        val length = end - start
        if (length > 8) {
            for (i in start + 2 until end - 2) {
                sb[i] = '*'
            }
        } else {
            for (i in start until end) {
                sb[i] = '*'
            }
        }
    }
}

