package com.alchemist.circle.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    private const val GITHUB_REPO = "ByMsKN/2048Circle"
    const val CURRENT_VERSION_TAG = "v3.0.0"

    data class UpdateInfo(
        val newVersionTag: String,
        val downloadUrl: String,
        val releaseNotes: String
    )

    /**
     * GitHub Releases API'sini sorgulayarak yeni sürüm olup olmadığını denetler.
     */
    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$GITHUB_REPO/releases/latest")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val latestTag = json.getString("tag_name")
                val body = json.optString("body", "Yeni özellikler ve hata düzeltmeleri!")

                // Sürüm etiketi farklıysa güncelleme var demektir
                if (latestTag != CURRENT_VERSION_TAG) {
                    val assets = json.getJSONArray("assets")
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.getString("name")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            val downloadUrl = asset.getString("browser_download_url")
                            return@withContext UpdateInfo(latestTag, downloadUrl, body)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    /**
     * APK'yı indirir ve ilerleme yüzdesini callback ile bildirir.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                instanceFollowRedirects = true
            }

            // GitHub release redirect (302) yönetimi
            var finalConnection = connection
            if (connection.responseCode in 300..399) {
                val redirectUrl = connection.getHeaderField("Location")
                finalConnection = (URL(redirectUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 15000
                }
            }

            val totalBytes = finalConnection.contentLength
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val outputFile = File(cacheDir, "SimyaCemberi_update.apk")
            if (outputFile.exists()) outputFile.delete()

            finalConnection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead: Long = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            val progress = totalRead.toFloat() / totalBytes.toFloat()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                }
            }
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * İndirilen APK'nın Android sistem kurulum ekranını başlatır.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
