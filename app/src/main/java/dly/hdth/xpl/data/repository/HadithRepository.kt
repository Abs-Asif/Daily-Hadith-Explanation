package dly.hdth.xpl.data.repository

import android.content.Context
import dly.hdth.xpl.data.model.Hadith
import dly.hdth.xpl.data.parser.HadithParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HadithRepository(private val context: Context) {

    private val okHttpClient = OkHttpClient()
    private val cacheDir = File(context.cacheDir, "hadith_cache").apply { mkdirs() }

    // Date format for DDMMYY (e.g. 160826 for 16 August 2026)
    private val dateFormat = SimpleDateFormat("ddMMyy", Locale.US)

    // Base URL to fetch straight from repo (https://github.com/Abs-Asif/Daily-Hadith-Explanation)
    private var baseUrl: String = "https://raw.githubusercontent.com/Abs-Asif/Daily-Hadith-Explanation/main/hadith/"

    fun setBaseUrl(url: String) {
        baseUrl = url
    }

    fun getTodayDateCode(): String {
        return dateFormat.format(Date())
    }

    suspend fun getAvailableFileList(): List<String> = withContext(Dispatchers.IO) {
        val cachedListFile = File(cacheDir, "list.txt")
        val remoteUrl = "${baseUrl}list.txt"
        val request = Request.Builder().url(remoteUrl).build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val content = response.body?.string() ?: ""
                if (content.isNotBlank()) {
                    cachedListFile.writeText(content)
                    return@withContext parseListFile(content)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Cache fallback
        if (cachedListFile.exists() && cachedListFile.length() > 0) {
            return@withContext parseListFile(cachedListFile.readText())
        }

        // Asset fallback
        val assetContent = loadAssetFile("list.txt")
        if (assetContent != null) {
            cachedListFile.writeText(assetContent)
            return@withContext parseListFile(assetContent)
        }

        emptyList()
    }

    private fun parseListFile(content: String): List<String> {
        return content.lines()
            .map { it.trim() }
            .filter { it.endsWith(".md", ignoreCase = true) }
    }

    suspend fun getHadithByFilename(fileName: String): Hadith = withContext(Dispatchers.IO) {
        val dateCode = fileName.removeSuffix(".md")
        val cachedFile = File(cacheDir, fileName)
        if (cachedFile.exists() && cachedFile.length() > 0) {
            val content = cachedFile.readText()
            val parsed = HadithParser.parse(dateCode, content)
            if (!parsed.isPlaceholder) {
                return@withContext parsed
            }
        }

        // Local assets fallback
        val assetContent = loadAssetFile(fileName)
        if (assetContent != null) {
            cachedFile.writeText(assetContent)
            return@withContext HadithParser.parse(dateCode, assetContent)
        }

        // Remote fetch
        val remoteUrl = "$baseUrl$fileName"
        val request = Request.Builder().url(remoteUrl).build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val content = response.body?.string() ?: ""
                if (content.isNotEmpty()) {
                    cachedFile.writeText(content)
                    return@withContext HadithParser.parse(dateCode, content)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        HadithParser.createPlaceholder(dateCode)
    }

    suspend fun getHadithForDate(dateCode: String): Hadith {
        return getHadithByFilename("$dateCode.md")
    }

    suspend fun getAllHadithsFromList(): List<Hadith> = withContext(Dispatchers.IO) {
        val fileList = getAvailableFileList()
        fileList.map { fileName ->
            getHadithByFilename(fileName)
        }.filter { !it.isPlaceholder }
    }

    private fun loadAssetFile(fileName: String): String? {
        return try {
            context.assets.open("hadith/$fileName").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }
}
