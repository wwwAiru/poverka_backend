package com.poverka.domain.web.routes

import kotlinx.html.*

fun renderPoverkaPage(uuid: String, thumbnails: List<String>, originals: List<String>): HTML.() -> Unit = {
    head {
        title("Poverka - $uuid")
    }
    body {
        h1 { +"Poverka $uuid" }
        thumbnails.forEachIndexed { index, thumb ->
            div {
                img(src = thumb, alt = "Thumbnail $index")
                a(href = originals[index]) { +"View original" }
            }
        }
    }
}
