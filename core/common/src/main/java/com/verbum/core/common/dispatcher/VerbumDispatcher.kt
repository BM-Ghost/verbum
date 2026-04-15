package com.verbum.core.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val verbumDispatcher: VerbumDispatcher)

enum class VerbumDispatcher {
    IO,
    Default,
    Main
}
