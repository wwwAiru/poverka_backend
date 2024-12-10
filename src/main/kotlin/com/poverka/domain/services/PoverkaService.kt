package com.poverka.domain.services

import com.poverka.domain.dto.FileMetadata
import java.io.File

class PoverkaService(private val storageService: StorageService) {

    fun getServerFileMetadata(clientUUIDsSet: Set<String>): List<FileMetadata> {
        val metadataList = mutableListOf<FileMetadata>()

        for (uuid in clientUUIDsSet) {
            val fileDir = File("uploads/$uuid")
            if (!fileDir.exists()) continue

            fileDir.listFiles()?.forEach { file ->
                metadataList.add(FileMetadata(file.name, file.lastModified()))
            }
        }

        return metadataList
    }



    fun saveJson(uuid: String, jsonData: String) {
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
