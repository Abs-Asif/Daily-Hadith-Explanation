package dly.hdth.xpl.data.parser

import dly.hdth.xpl.data.model.Hadith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithParserTest {

    @Test
    fun testParseValidJsonAndMarkdown() {
        val sample = """
            {
              "arabic": "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
              "bangla": "সকল কাজ নিয়তের উপর নির্ভরশীল।"
            }
            # হাদীসের গুরুত্ব
            নিয়তের বিশুদ্ধতা দ্বীনের মূল বিষয়।
        """.trimIndent()

        val hadith = HadithParser.parse("160826", sample)

        assertEquals("160826", hadith.dateCode)
        assertEquals("إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ", hadith.arabic)
        assertEquals("সকল কাজ নিয়তের উপর নির্ভরশীল।", hadith.bangla)
        assertTrue(hadith.explanationMarkdown.contains("নিয়তের বিশুদ্ধতা দ্বীনের মূল বিষয়।"))
        assertFalse(hadith.isPlaceholder)
    }

    @Test
    fun testParseEmptyContentReturnsPlaceholder() {
        val hadith = HadithParser.parse("160826", "")
        assertTrue(hadith.isPlaceholder)
        assertTrue(hadith.bangla.contains("আমাদের অনুবাদক আজ ব্যাস্ত"))
    }
}
