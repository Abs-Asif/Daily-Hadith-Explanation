package dly.hdth.xpl.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dly.hdth.xpl.ui.theme.RuposhiBanglaFontFamily
import dly.hdth.xpl.ui.theme.ScheherazadeFontFamily

@Composable
fun SimpleMarkdownViewer(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val lines = markdownText.lines()
    Column(modifier = modifier.fillMaxWidth()) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# ").trim(),
                        fontFamily = RuposhiBanglaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## ").trim(),
                        fontFamily = RuposhiBanglaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### ").trim(),
                        fontFamily = RuposhiBanglaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Text(
                        text = "• " + line.substring(2).trim(),
                        fontFamily = RuposhiBanglaFontFamily,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
                else -> {
                    Text(
                        text = line,
                        fontFamily = RuposhiBanglaFontFamily,
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

private fun isArabicText(text: String): Boolean {
    val arabicChars = text.count { char ->
        char in '\u0600'..'\u06FF' || char in '\u0750'..'\u077F' || char in '\u08A0'..'\u08FF' || char in '\uFB50'..'\uFDFF' || char in '\uFE70'..'\uFEFF'
    }
    return arabicChars > 0 && arabicChars.toDouble() / text.length > 0.3
}
