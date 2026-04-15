package com.verbum.core.common.constants

object VerbumConstants {
    const val BIBLE_DATABASE_NAME = "verbum_bible.db"
    const val APP_DATABASE_NAME = "verbum_app.db"
    const val DATASTORE_NAME = "verbum_preferences"

    const val BIBLE_BOOKS_OT = 46 // Catholic Old Testament
    const val BIBLE_BOOKS_NT = 27
    const val BIBLE_BOOKS_TOTAL = 73

    const val AI_MAX_TOKENS = 2048
    const val AI_TEMPERATURE = 0.7f
    const val AI_SYSTEM_PROMPT_KEY = "verbum_ai_system"

    const val PAGE_SIZE = 20
    const val COMMUNITY_FEED_PAGE_SIZE = 30

    object DeepLink {
        const val SCHEME = "verbum"
        const val HOST = "app"
        const val VERSE_PATH = "verse"
        const val MISSAL_PATH = "missal"
        const val COMMUNITY_PATH = "community"
    }
}
