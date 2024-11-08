package com.connor.hindsightmobile.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun convertToLocalTime(timestamp: Long): String {
    // Define the formatter for the desired date-time format
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    // Convert the timestamp to LocalDateTime using the system's default time zone
    val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())

    // Format the LocalDateTime to a string
    return localDateTime.format(formatter)
}