package com.poverka.domain.web.routes

import kotlinx.html.*
import java.io.File

fun HTML.renderPoverkaPage(uuid: String, files: List<File>, columnsClass: String) {
    head {
        title { +"Gallery for UUID: $uuid" }
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1.0"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "/static/css/styles.css"
        }
    }
    body {
        h1 { +"Gallery for UUID: $uuid" }
        div(classes = "gallery $columnsClass") { // Теперь применяем переданный класс
            files.filter { it.name.startsWith("thumb_") }.forEach { thumbFile ->
                val originalFile = files.find { it.name == thumbFile.name.removePrefix("thumb_") }
                if (originalFile != null) {
                    div(classes = "gallery-item") {
                        a(href = "/uploads/$uuid/${originalFile.name}") {
                            img(src = "/uploads/$uuid/${thumbFile.name}", alt = thumbFile.name)
                        }
                    }
                }
            }
        }
    }
}
