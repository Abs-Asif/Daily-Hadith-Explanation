package dly.hdth.xpl.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import kotlin.concurrent.thread

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

    @Test
    fun testListParsingWithSingleRecord() {
        val sampleListContent = "160826.md\n"
        val lines = sampleListContent.lines()
            .map { it.trim() }
            .filter { it.endsWith(".md", ignoreCase = true) }

        assertEquals(1, lines.size)
        assertEquals("160826.md", lines[0])
    }

    @Test
    fun testMockRemoteFetchList() {
        val serverSocket = ServerSocket(0)
        val port = serverSocket.localPort

        thread {
            try {
                val clientSocket = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                while (reader.readLine()?.isNotEmpty() == true) {
                    // Consume HTTP headers
                }
                val writer = PrintWriter(clientSocket.getOutputStream())
                val body = "160826.md\n"
                writer.print("HTTP/1.1 200 OK\r\n")
                writer.print("Content-Length: ${body.length}\r\n")
                writer.print("Content-Type: text/plain\r\n")
                writer.print("Connection: close\r\n\r\n")
                writer.print(body)
                writer.flush()
                clientSocket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val url = "http://localhost:$port/hadith/list.txt"
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val body = response.body?.string() ?: ""
        serverSocket.close()

        val parsed = body.lines().map { it.trim() }.filter { it.endsWith(".md", ignoreCase = true) }
        assertEquals(1, parsed.size)
        assertEquals("160826.md", parsed[0])
    }
}
