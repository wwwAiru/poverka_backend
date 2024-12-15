package com.poverka

import com.poverka.domain.services.StorageService
import com.poverka.domain.services.PoverkaService
import com.poverka.domain.web.poverkaRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

fun main(args: Array<String>) {
    embeddedServer(Netty, host = "0.0.0.0", port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    val storageService = StorageService(File("uploads"))
    val poverkaService = PoverkaService(storageService)

    routing {
        poverkaRoutes(poverkaService, storageService)
    }
}
