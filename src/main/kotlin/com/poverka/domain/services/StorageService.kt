package com.poverka.domain.services

import java.io.File
import java.io.IOException

class StorageService(private val storageDir: File) {

    init {
        // Создаем корневую директорию, если она не существует
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

    // Метод для сохранения файлов
    fun storeFiles(uuid: String, files: Map<String, ByteArray>) {
        val uuidDir = File(storageDir, uuid)
        if (!uuidDir.exists()) {
            uuidDir.mkdirs()
        }

        files.forEach { (fileName, fileBytes) ->
            try {
                val file = File(uuidDir, fileName)

                // Сохраняем файл
                file.writeBytes(fileBytes)
            } catch (e: IOException) {
                println("Ошибка сохранения файла '$fileName': ${e.message}")
                throw e
            }
        }
    }

    // Метод для получения файлов по UUID
    fun getFiles(uuid: String): List<File> {
        val uuidDir = File(storageDir, uuid)
        return if (uuidDir.exists()) {
            uuidDir.listFiles()?.toList() ?: emptyList() // Возвращаем список файлов в директории UUID
        } else {
            emptyList() // Если директория не существует, возвращаем пустой список
        }
    }
}
