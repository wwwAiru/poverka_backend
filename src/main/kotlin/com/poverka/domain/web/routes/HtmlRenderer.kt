package com.poverka.domain.web.routes

import kotlinx.html.*
//TODO()
fun renderPoverkaPage(uuid: String, thumbnails: List<String>, originals: List<String>): HTML.() -> Unit = {
    head {
        title("Poverka - $uuid")
    }
    body {
        h1 { +"Poverka $uuid" }

        // Печатаем для отладки
        p { +"Thumbnails count: ${thumbnails.size}" }
        p { +"Originals count: ${originals.size}" }

        if (thumbnails.isEmpty() || originals.isEmpty()) {
            p { +"No images available for $uuid." }
        } else {
            thumbnails.zip(originals).forEachIndexed { index, (thumb, original) ->
                div {
                    img(
                        src = "/uploads/$uuid/$thumb",
                        alt = "Thumbnail $index",
                        classes = "thumbnail"
                    )
                    a(
                        href = "/uploads/$uuid/$original",
                        target = "_blank",
                    ) { +"View original" }
                }
            }
        }
    }
}
