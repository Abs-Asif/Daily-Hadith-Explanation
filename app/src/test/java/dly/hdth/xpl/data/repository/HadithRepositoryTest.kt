package dly.hdth.xpl.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class HadithRepositoryTest {

    @Test
    fun testParseListFileContent() {
        val sampleListContent = """
            150826.md
            160826.md
            170826.md
            invalid_file.txt

        """.trimIndent()

        val lines = sampleListContent.lines()
            .map { it.trim() }
            .filter { it.endsWith(".md", ignoreCase = true) }

        assertEquals(3, lines.size)
        assertEquals("150826.md", lines[0])
        assertEquals("160826.md", lines[1])
        assertEquals("170826.md", lines[2])
    }
}
