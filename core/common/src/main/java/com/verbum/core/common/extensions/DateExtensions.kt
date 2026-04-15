package com.verbum.core.common.extensions

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDate.toReadableDate(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
    return format(formatter)
}

fun LocalDate.toShortDate(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    return format(formatter)
}

fun LocalDate.toLiturgicalKey(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    return format(formatter)
}
