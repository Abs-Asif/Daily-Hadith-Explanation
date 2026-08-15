package dly.hdth.xpl.data.parser

import com.google.gson.Gson
import com.google.gson.JsonObject
import dly.hdth.xpl.data.model.Hadith

object HadithParser {

    private val gson = Gson()

    fun parse(dateCode: String, fileContent: String): Hadith {
        val trimmed = fileContent.trim()
        if (trimmed.isEmpty()) {
            return createPlaceholder(dateCode)
        }

        // Check if file starts with JSON block object '{' or '```json' or frontmatter
        val jsonString: String
        val markdownContent: String

        if (trimmed.startsWith("{")) {
            // Find closing brace of JSON block
            var openBraces = 0
            var closeIndex = -1
            var inString = false
            var escape = false

            for (i in trimmed.indices) {
                val c = trimmed[i]
                if (escape) {
                    escape = false
                    continue
                }
                if (c == '\\') {
                    escape = true
                    continue
                }
                if (c == '"') {
                    inString = !inString
                    continue
                }
                if (!inString) {
                    if (c == '{') openBraces++
                    else if (c == '}') {
                        openBraces--
                        if (openBraces == 0) {
                            closeIndex = i
                            break
                        }
                    }
                }
            }

            if (closeIndex != -1) {
                jsonString = trimmed.substring(0, closeIndex + 1)
                markdownContent = trimmed.substring(closeIndex + 1).trim()
            } else {
                jsonString = trimmed
                markdownContent = ""
            }
        } else if (trimmed.startsWith("```json")) {
            val endFenceIndex = trimmed.indexOf("```", 7)
            if (endFenceIndex != -1) {
                jsonString = trimmed.substring(7, endFenceIndex).trim()
                markdownContent = trimmed.substring(endFenceIndex + 3).trim()
            } else {
                jsonString = trimmed.removePrefix("```json").trim()
                markdownContent = ""
            }
        } else {
            // Fallback: entire file as markdown content if no json structure
            return Hadith(
                dateCode = dateCode,
                arabic = "",
                bangla = trimmed,
                explanationMarkdown = trimmed
            )
        }

        return try {
            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
            val arabic = jsonObject.get("arabic")?.asString ?: ""
            val bangla = jsonObject.get("bangla")?.asString ?: ""

            Hadith(
                dateCode = dateCode,
                arabic = arabic,
                bangla = bangla,
                explanationMarkdown = markdownContent
            )
        } catch (e: Exception) {
            e.printStackTrace()
            createPlaceholder(dateCode)
        }
    }

    fun createPlaceholder(dateCode: String): Hadith {
        return Hadith(
            dateCode = dateCode,
            arabic = "",
            bangla = "আমাদের অনুবাদক আজ ব্যাস্ত, তাই কোনো হাদীস নেই। পরে আপনাকে জানিয়ে দেয়া হবে।",
            explanationMarkdown = "আমাদের অনুবাদক আজ ব্যাস্ত, তাই কোনো হাদীস নেই। পরে আপনাকে জানিয়ে দেয়া হবে।",
            isPlaceholder = true
        )
    }
}
