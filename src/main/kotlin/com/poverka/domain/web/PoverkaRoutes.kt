package com.poverka.domain.web

import com.poverka.domain.services.PoverkaService
import com.poverka.domain.services.StorageService
import com.poverka.domain.utils.DeviceUtils
import com.poverka.domain.web.routes.renderPoverkaPage
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.poverkaRoutes(poverkaService: PoverkaService, storageService: StorageService) {

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

    // Получение списка файлов для сравнения
    post("/files") {
        try {
            val receivedUuids = call.receive<Set<String>>()
            if (receivedUuids.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Список UUID пуст.")
                return@post
            }

            call.application.log.info("Получены UUID: $receivedUuids")

            val filesMetadata = poverkaService.getServerFileMetadata(receivedUuids)

            call.respond(HttpStatusCode.OK, filesMetadata)
        } catch (e: Exception) {
            call.application.log.error("Ошибка при обработке запроса: ${e.localizedMessage}", e)
            call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере.")
        }
    }

    // Получение страницы с фотографиями поверки
    get("/gallery/{uuid}") {
        val uuid = call.parameters["uuid"] ?: return@get call.respondText("UUID not found", status = io.ktor.http.HttpStatusCode.NotFound)
        val files = storageService.getFiles(uuid)
        val userAgent = call.request.headers["User-Agent"]
        val isMobile = DeviceUtils.isMobileDevice(userAgent)

        // Для мобильных 2 колонки, для других 3
        val columnsClass = if (isMobile) "gallery-2" else "gallery-3"

        call.respondHtml {
            renderPoverkaPage(uuid, files, columnsClass)
        }
    }






    get("/ping") {
        call.respondText("OK", status = HttpStatusCode.OK)
    }

    staticFiles("/static", File("src/main/resources/static"))
    staticFiles("/uploads", File("uploads"))
}
