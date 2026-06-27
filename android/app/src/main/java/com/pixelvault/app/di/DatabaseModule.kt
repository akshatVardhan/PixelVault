package com.pixelvault.app.di

import android.content.Context
import androidx.room.Room
import com.pixelvault.app.data.local.AppDatabase
import com.pixelvault.app.data.local.ClusterDao
import com.pixelvault.app.data.local.FaceDao
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pixelvault.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun providePhotoDao(db: AppDatabase): PhotoDao = db.photoDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()

    @Provides
    @Singleton
    fun provideFaceDao(db: AppDatabase): FaceDao = db.faceDao()

    @Provides
    @Singleton
    fun provideClusterDao(db: AppDatabase): ClusterDao = db.clusterDao()
}
