package com.nomono.sono.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private fun groupThousands(digits: String): String {
    val sb = StringBuilder()
    val n = digits.length
    for (i in 0 until n) {
        sb.append(digits[i])
        val remaining = n - i - 1
        if (remaining > 0 && remaining % 3 == 0) sb.append('.')
    }
    return sb.toString()
}

object VndGroupingTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter(Char::isDigit)
        if (digits.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        val grouped = groupThousands(digits)
        val n = digits.length
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, n)
                var count = 0
                for (i in 0 until o) {
                    val remaining = n - i - 1
                    if (remaining > 0 && remaining % 3 == 0) count++
                }
                return o + count
            }

            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, grouped.length)
                var original = 0
                var i = 0
                while (i < o) {
                    if (grouped[i] != '.') original++
                    i++
                }
                return original
            }
        }
        return TransformedText(AnnotatedString(grouped), offsetMapping)
    }
}
