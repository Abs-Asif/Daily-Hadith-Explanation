package dly.hdth.xpl.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleMarkdownViewerTest {

    @Test
    fun testParseInlineMarkdownBoldAndItalic() {
        val input = "**তথ্যসূত্র:** তিরমিযী (১৩২১)"
        val parsed = parseInlineMarkdown(input)
        assertEquals("তথ্যসূত্র: তিরমিযী (১৩২১)", parsed.text)
    }

    @Test
    fun testParseInlineMarkdownHeaderContent() {
        val input = "**হাদিসের সংক্ষিপ্ত ব্যাখ্যা (مختصر شرح الحديث)**"
        val parsed = parseInlineMarkdown(input)
        assertEquals("হাদিসের সংক্ষিপ্ত ব্যাখ্যা (مختصر شرح الحديث)", parsed.text)
    }
}
