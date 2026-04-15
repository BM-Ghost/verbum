package com.verbum.core.common.extensions

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DateExtensionsTest {

    @Test
    fun `toLiturgicalKey uses stable ISO style output`() {
        val date = LocalDate.of(2026, 4, 15)

        assertEquals("2026-04-15", date.toLiturgicalKey())
    }

    @Test
    fun `toShortDate uses month and day`() {
        val date = LocalDate.of(2026, 12, 25)

        assertEquals("Dec 25", date.toShortDate())
    }
}
