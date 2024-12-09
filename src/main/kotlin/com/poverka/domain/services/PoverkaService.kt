package com.poverka.domain.services

import java.io.File

class PoverkaService(private val storageService: StorageService) {

    suspend fun saveJson(uuid: String, jsonData: String) {
        val jsonFile = File(storageService.storageDirectory, "$uuid/data.json")
        jsonFile.parentFile?.mkdirs() // Создаем директорию, если её нет
        jsonFile.writeText(jsonData) // Сохраняем JSON
    }

    suspend fun storeFiles(uuid: String, files: MutableMap<String, ByteArray>) {
        storageService.storeFiles(uuid, files)
    }

    suspend fun getFilesForPoverka(uuid: String): List<File> {
        return storageService.getFiles(uuid)
    }
}
