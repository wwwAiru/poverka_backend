package com.poverka.domain.utils

import java.util.*


object DeviceUtils {
    fun isMobileDevice(userAgent: String?): Boolean {
        val mobileAgents = listOf(
            "mobile", "android", "silk/", "kindle", "blackberry", "opera mini", "opera mobi"
        )
        return userAgent?.lowercase(Locale.getDefault())?.let { agent ->
            mobileAgents.any { it in agent }
        } ?: false
    }
}