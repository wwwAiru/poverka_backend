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


    fun storeFiles(uuid: String, files: MutableMap<String, ByteArray>) {
        storageService.storeFiles(uuid, files)
    }

}
