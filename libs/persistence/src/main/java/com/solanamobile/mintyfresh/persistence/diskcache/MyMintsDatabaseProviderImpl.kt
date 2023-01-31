package com.solanamobile.mintyfresh.persistence.diskcache

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsDatabaseProviderImpl @Inject constructor(@ApplicationContext applicationContext: Context) :
    MyMintsDatabaseProvider {
    override val roomDb = Room.databaseBuilder(
        applicationContext,
        MyMintsDatabase::class.java,
        "myMintsDb",
    ).addMigrations(MIGRATION_1_2).build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MyMintsDatabaseProviderModule {
    @Singleton
    @Binds
    internal abstract fun bindMyMintsDatabaseProvider(
        myMintsDatabaseProviderImpl: MyMintsDatabaseProviderImpl
    ): MyMintsDatabaseProvider
}
