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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HadithRepository(private val context: Context) {

    private val okHttpClient = OkHttpClient()
    private val cacheDir = File(context.cacheDir, "hadith_cache").apply { mkdirs() }

    // Date format for DDMMYY (e.g. 160826 for 16 August 2026)
    private val dateFormat = SimpleDateFormat("ddMMyy", Locale.US)

    // Base URL to fetch from repo raw files (or local fallback)
    private var baseUrl: String = "https://raw.githubusercontent.com/user/repo/main/hadith/"

    fun setBaseUrl(url: String) {
        baseUrl = url
    }

    fun getTodayDateCode(): String {
        return dateFormat.format(Date())
    }

    suspend fun getHadithForDate(dateCode: String): Hadith = withContext(Dispatchers.IO) {
        val cachedFile = File(cacheDir, "$dateCode.md")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            val content = cachedFile.readText()
            val parsed = HadithParser.parse(dateCode, content)
            if (!parsed.isPlaceholder) {
                return@withContext parsed
            }
        }

        // Check local assets / local hadith folder fallback first
        val assetContent = loadFromAssets(dateCode)
        if (assetContent != null) {
            cachedFile.writeText(assetContent)
            return@withContext HadithParser.parse(dateCode, assetContent)
        }

        // Fetch remotely
        val remoteUrl = "$baseUrl$dateCode.md"
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

        // If fetch failed and no cached non-placeholder file, return placeholder
        HadithParser.createPlaceholder(dateCode)
    }

    suspend fun getPreviousDaysHadiths(count: Int = 15): List<Hadith> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Hadith>()
        val calendar = Calendar.getInstance()

        for (i in 1..count) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val dateCode = dateFormat.format(calendar.time)
            val hadith = getHadithForDate(dateCode)
            if (!hadith.isPlaceholder) {
                list.add(hadith)
            }
        }
        list
    }

    private fun loadFromAssets(dateCode: String): String? {
        return try {
            context.assets.open("hadith/$dateCode.md").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }
}
