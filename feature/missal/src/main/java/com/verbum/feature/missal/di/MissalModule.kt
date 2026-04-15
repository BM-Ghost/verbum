package com.verbum.feature.missal.di

import com.verbum.feature.missal.data.MissalRepository
import com.verbum.feature.missal.data.MissalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MissalModule {

    @Binds
    @Singleton
    abstract fun bindMissalRepository(impl: MissalRepositoryImpl): MissalRepository
}
