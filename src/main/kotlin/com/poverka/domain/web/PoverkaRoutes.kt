package com.poverka.domain.web

import com.poverka.domain.services.PoverkaService
import com.poverka.domain.web.routes.renderPoverkaPage
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

fun Route.poverkaRoutes(poverkaService: PoverkaService) {

    // Загрузка JSON
    route("/upload/json") {
        post {
            try {
                val jsonData = call.receiveText()
                val jsonObject = Json.decodeFromString<JsonObject>(jsonData)
                val uuid = jsonObject["uuid"]?.jsonPrimitive?.content

                if (uuid.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "UUID отсутствует в JSON.")
                    return@post
                }

                poverkaService.saveJson(uuid, jsonData)
                call.respond(HttpStatusCode.OK, "JSON успешно сохранён.")
            } catch (e: Exception) {
                call.application.log.error("Ошибка при сохранении JSON: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере.")
            }
        }
    }

    // Загрузка фото
    route("/upload/photo") {
        post {
            try {
                val multipart = call.receiveMultipart()
                val files = mutableMapOf<String, ByteArray>()
                var uuid: String? = null

                call.application.log.info("Начало обработки запроса на загрузку фото.")

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val fileName = part.originalFileName?.replace(Regex("[^a-zA-Z0-9._-]"), "_") ?: "unnamed"
                            val bytes = part.streamProvider().readBytes()
                            files[fileName] = bytes
                            call.application.log.info("Получен файл: $fileName, размер: ${bytes.size} байт.")
                        }
                        is PartData.FormItem -> {
                            if (part.name == "uuid") {
                                uuid = part.value
                                call.application.log.info("Получен UUID: $uuid.")
                            }
                        }
                        else -> call.application.log.info("Пропущена неизвестная часть: $part.")
                    }
                    part.dispose()
                }

                if (uuid.isNullOrBlank()) {
                    call.application.log.warn("UUID не передан.")
                    call.respond(HttpStatusCode.BadRequest, "UUID не передан.")
                    return@post
                }

                if (files.isEmpty()) {
                    call.application.log.warn("Файлы не переданы.")
                    call.respond(HttpStatusCode.BadRequest, "Файлы не переданы.")
                    return@post
                }

                val fileDir = File("uploads/$uuid")
                if (!fileDir.exists()) {
                    fileDir.mkdirs()
                }

                poverkaService.storeFiles(uuid!!, files)

                call.application.log.info("Файлы успешно сохранены для UUID $uuid.")
                call.respond(HttpStatusCode.OK, "Файлы успешно загружены.")
            } catch (e: Exception) {
                call.application.log.error("Ошибка при обработке загрузки: ${e.localizedMessage}", e)
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }
    }

    // Получение файлов
    get("/{uuid}") {
        val uuid = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val files = poverkaService.getFilesForPoverka(uuid)

        if (files.isEmpty()) {
            call.application.log.warn("Файлы для UUID $uuid не найдены.")
            return@get call.respond(HttpStatusCode.NotFound, "Файлы не найдены.")
        }

        val (thumbnails, originals) = files.partition { it.name.startsWith("thumb_") }

        // Логирование содержимого
        call.application.log.info("Thumbnails: ${thumbnails.map { it.name }}")
        call.application.log.info("Originals: ${originals.map { it.name }}")

        if (thumbnails.isEmpty() || originals.isEmpty()) {
            call.application.log.warn("Тumbnails или Originals пустые.")
        }

        call.respondHtml(HttpStatusCode.OK) {
            renderPoverkaPage(
                uuid,
                thumbnails.map { it.name },
                originals.map { it.name }
            )
        }
    }
}
