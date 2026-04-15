package com.verbum.feature.bible.di

import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.data.BibleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BibleModule {

    @Binds
    @Singleton
    abstract fun bindBibleRepository(impl: BibleRepositoryImpl): BibleRepository
}
