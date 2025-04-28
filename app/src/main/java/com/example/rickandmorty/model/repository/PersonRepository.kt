package com.example.rickandmorty.model.repository

import android.content.Context
import android.util.Log
import com.example.rickandmorty.api.client.FirebaseApiClient
import com.example.rickandmorty.model.dao.PersonDao
import com.example.rickandmorty.model.entity.Person
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class PersonRepository(
    private val apiClient: FirebaseApiClient,
    private val imageDao: PersonDao,
    private val context: Context
) {
    private val imagesDir by lazy {
        File(context.filesDir, "images2").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun fetchAndSaveImages() {
        val tempDir = File(context.filesDir, "temp_images").apply { mkdirs() }
        try {
            val remotePersons = apiClient.getPersons()
            val currentPersonIds = imageDao.getAllImagesOnce().map { it.id }

            // Устанавливаем состояние загрузки для существующих персонажей
            setLoadingStatus(currentPersonIds, true)

            // 1. Скачиваем изображения
            remotePersons.forEach { person ->
                val tempFile = File(tempDir, "${person.id}.jpg")
                try {
                    downloadImage(person.imageUrl, tempFile)
                } catch (e: Exception) {
                    Log.e("Download", "Failed to download image for ${person.id}", e)
                }
            }

            // 2. Обновляем файлы
            imagesDir.listFiles()?.forEach { it.delete() }
            tempDir.listFiles()?.forEach { file ->
                file.copyTo(File(imagesDir, file.name), overwrite = true)
            }

            // 3. Обновляем БД
            remotePersons.forEach { person ->
                imageDao.upsertImage(
                    Person(
                        id = person.id,
                        name = person.name,
                        filePath = File(imagesDir, "${person.id}.jpg").absolutePath,
                        status = person.status,
                        species = person.species,
                        isLoading = false
                    )
                )
            }

        } catch (e: Exception) {
            // В случае ошибки сбрасываем состояние загрузки
            val currentPersonIds = imageDao.getAllImagesOnce().map { it.id }
            setLoadingStatus(currentPersonIds, false)
            throw e
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private suspend fun setLoadingStatus(ids: List<UUID>, isLoading: Boolean) {
        val currentPersons = imageDao.getAllImagesOnce()
            .filter { it.id in ids }
            .map { it.copy(isLoading = isLoading) }
        currentPersons.forEach { person ->
            imageDao.updatePerson(person)
        }
    }

    private suspend fun downloadImage(url: String, outputFile: File) {
        try {
            HttpClient(OkHttp).use { client ->
                val response: HttpResponse = client.get(url)
                if (response.status.isSuccess()) {
                    response.body<ByteArray>().let { bytes ->
                        withContext(Dispatchers.IO) {
                            outputFile.parentFile?.mkdirs()
                            outputFile.writeBytes(bytes)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Download", "Failed to download image", e)
            throw e // Или обработайте ошибку
        }
    }

    fun getLocalImages(): Flow<List<Person>> {
        return imageDao.getAllImages()
    }
}

/*
suspend fun fetchAndSaveImages() {
        val tempDir = File(context.filesDir, "temp_images").apply { mkdirs() }
        try {
            val remotePersons = apiClient.getPersons()

            // 1. Скачиваем во временную папку
            remotePersons.forEach { person ->
                val tempFile = File(tempDir, "${person.id}.jpg")
                downloadImage(person.imageUrl, tempFile)
            }

            // 2. Если все скачалось - заменяем старые файлы
            imagesDir.listFiles()?.forEach { it.delete() } // Очищаем основную папку
            tempDir.listFiles()?.forEach { file ->
                file.copyTo(File(imagesDir, file.name), overwrite = true)
            }

            // 3. Обновляем БД
            remotePersons.forEach { person ->
                imageDao.upsertImage(
                    Person(
                        id = person.id,
                        name = person.name,
                        filePath = File(imagesDir, "${person.id}.jpg").absolutePath,
                        status = person.status,
                        species = person.species
                    )
                )
            }

        } finally {
            tempDir.deleteRecursively() // Удаляем временную папку в любом случае
        }
    }
 */