package dly.hdth.xpl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dly.hdth.xpl.ui.theme.EkusheyLalsaluFontFamily
import dly.hdth.xpl.ui.theme.ScheherazadeFontFamily

@Composable
fun SimpleMarkdownViewer(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val lines = markdownText.lines()
    var inCodeBlock = false
    val codeBlockContent = mutableListOf<String>()

    Column(modifier = modifier.fillMaxWidth()) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()

            // Handle code block fences ```
            if (line.startsWith("```")) {
                if (inCodeBlock) {
                    // End code block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = codeBlockContent.joinToString("\n"),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                    codeBlockContent.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                return@forEach
            }

            if (inCodeBlock) {
                codeBlockContent.add(rawLine)
                return@forEach
            }

            when {
                // Horizontal rule: ---, ***, ___
                line.matches(Regex("^(\\-{3,}|\\*{3,}|_{3,})$")) -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // Headers (# H1, ## H2, ### H3, #### H4)
                line.startsWith("#") -> {
                    val headerLevel = line.takeWhile { it == '#' }.length
                    val headerContent = line.drop(headerLevel).trim()
                    val (fontSize, fontWeight, color) = when (headerLevel) {
                        1 -> Triple(22.sp, FontWeight.Bold, MaterialTheme.colorScheme.primary)
                        2 -> Triple(20.sp, FontWeight.Bold, MaterialTheme.colorScheme.primary)
                        3 -> Triple(18.sp, FontWeight.Bold, MaterialTheme.colorScheme.onSurface)
                        else -> Triple(16.sp, FontWeight.Bold, MaterialTheme.colorScheme.onSurface)
                    }

                    Text(
                        text = parseInlineMarkdown(headerContent),
                        fontFamily = EkusheyLalsaluFontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        color = color,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Blockquotes (> text)
                line.startsWith(">") -> {
                    val quoteText = line.removePrefix(">").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(quoteText),
                            fontFamily = EkusheyLalsaluFontFamily,
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bullet lists (- item, * item, + item)
                line.startsWith("- ") || line.startsWith("* ") || line.startsWith("+ ") -> {
                    val itemText = line.substring(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            fontFamily = EkusheyLalsaluFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = parseInlineMarkdown(itemText),
                            fontFamily = EkusheyLalsaluFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Numbered lists (1. item, 2. item)
                line.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val dotIndex = line.indexOf('.')
                    val numberStr = line.substring(0, dotIndex + 1)
                    val itemText = line.substring(dotIndex + 1).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "$numberStr ",
                            fontFamily = EkusheyLalsaluFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = parseInlineMarkdown(itemText),
                            fontFamily = EkusheyLalsaluFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Blank lines
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Entirely Arabic line
                isArabicText(line) -> {
                    Text(
                        text = line,
                        fontFamily = ScheherazadeFontFamily,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )
                }

                // Default paragraph line
                else -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        fontFamily = EkusheyLalsaluFontFamily,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Justify,
                        lineHeight = 24.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length
        while (i < len) {
            when {
                // ***bold italic*** or ___bold italic___
                (text.startsWith("***", i) || text.startsWith("___", i)) -> {
                    val token = text.substring(i, i + 3)
                    val end = text.indexOf(token, i + 3)
                    if (end != -1) {
                        val inner = text.substring(i + 3, end)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(inner)
                        }
                        i = end + 3
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // **bold** or __bold__
                (text.startsWith("**", i) || text.startsWith("__", i)) -> {
                    val token = text.substring(i, i + 2)
                    val end = text.indexOf(token, i + 2)
                    if (end != -1) {
                        val inner = text.substring(i + 2, end)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(inner)
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // *italic* or _italic_
                (text[i] == '*' || text[i] == '_') -> {
                    val token = text[i].toString()
                    val end = text.indexOf(token, i + 1)
                    if (end != -1 && end > i + 1) {
                        val inner = text.substring(i + 1, end)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(inner)
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // `code`
                (text[i] == '`') -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1 && end > i + 1) {
                        val inner = text.substring(i + 1, end)
                        withStyle(SpanStyle(background = Color.LightGray.copy(alpha = 0.3f))) {
                            append(inner)
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

private fun isArabicText(text: String): Boolean {
    val arabicChars = text.count { char ->
        char in '\u0600'..'\u06FF' || char in '\u0750'..'\u077F' || char in '\u08A0'..'\u08FF' || char in '\uFB50'..'\uFDFF' || char in '\uFE70'..'\uFEFF'
    }
    return arabicChars > 0 && arabicChars.toDouble() / text.length > 0.3
}
