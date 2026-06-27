package com.pixelvault.app.di

import android.content.Context
import com.pixelvault.app.data.local.ClusterDao
import com.pixelvault.app.data.local.FaceDao
import com.pixelvault.app.ml.DelegateSelector
import com.pixelvault.app.ml.FaceClusterer
import com.pixelvault.app.ml.FaceDetector
import com.pixelvault.app.ml.FaceEmbedder
import com.pixelvault.app.ml.FoodClassifier
import com.pixelvault.app.ml.MLPipelineService
import com.pixelvault.app.ml.ModelLoader
import com.pixelvault.app.ml.SceneDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun provideDelegateSelector(@ApplicationContext ctx: Context) = DelegateSelector(ctx)

    @Provides
    @Singleton
    fun provideModelLoader(@ApplicationContext ctx: Context, selector: DelegateSelector) = ModelLoader(ctx, selector)

    @Provides
    @Singleton
    fun provideSceneDetector(@ApplicationContext ctx: Context, loader: ModelLoader) = SceneDetector(ctx, loader)

    @Provides
    @Singleton
    fun provideFoodClassifier(@ApplicationContext ctx: Context, loader: ModelLoader) = FoodClassifier(ctx, loader)

    @Provides
    @Singleton
    fun provideFaceDetector() = FaceDetector()

    @Provides
    @Singleton
    fun provideFaceEmbedder(loader: ModelLoader) = FaceEmbedder(loader)

    @Provides
    @Singleton
    fun provideFaceClusterer(faceDao: FaceDao, clusterDao: ClusterDao) = FaceClusterer(faceDao, clusterDao)

    @Provides
    @Singleton
    fun provideMLPipelineService(
        sceneDetector: SceneDetector,
        foodClassifier: FoodClassifier,
        faceDetector: FaceDetector,
        faceEmbedder: FaceEmbedder,
        faceClusterer: FaceClusterer,
        photoDao: com.pixelvault.app.data.local.PhotoDao,
        faceDao: FaceDao,
        tagDao: com.pixelvault.app.data.local.TagDao,
        contentResolver: android.content.ContentResolver
    ) = MLPipelineService(sceneDetector, foodClassifier, faceDetector, faceEmbedder, faceClusterer, photoDao, faceDao, tagDao, contentResolver)
}
