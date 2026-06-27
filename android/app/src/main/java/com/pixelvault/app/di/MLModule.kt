package com.pixelvault.app.di

import android.content.Context
import com.pixelvault.app.ml.*
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
    fun provideDelegateSelector(@ApplicationContext context: Context): DelegateSelector {
        return DelegateSelector(context)
    }

    @Provides
    @Singleton
    fun provideModelLoader(
        @ApplicationContext context: Context,
        delegateSelector: DelegateSelector
    ): ModelLoader {
        return ModelLoader(context, delegateSelector)
    }

    @Provides
    @Singleton
    fun provideSceneDetector(
        @ApplicationContext context: Context,
        modelLoader: ModelLoader
    ): SceneDetector {
        return SceneDetector(context, modelLoader)
    }

    @Provides
    @Singleton
    fun provideFoodClassifier(
        @ApplicationContext context: Context,
        modelLoader: ModelLoader
    ): FoodClassifier {
        return FoodClassifier(context, modelLoader)
    }

    @Provides
    @Singleton
    fun provideFaceDetector(): FaceDetector {
        return FaceDetector()
    }

    @Provides
    @Singleton
    fun provideFaceEmbedder(modelLoader: ModelLoader): FaceEmbedder {
        return FaceEmbedder(modelLoader)
    }

    @Provides
    @Singleton
    fun provideMLPipelineService(
        @ApplicationContext context: Context,
        sceneDetector: SceneDetector,
        foodClassifier: FoodClassifier,
        faceDetector: FaceDetector,
        faceEmbedder: FaceEmbedder,
        db: com.pixelvault.app.data.local.AppDatabase
    ): MLPipelineService {
        return MLPipelineService(context, sceneDetector, foodClassifier, faceDetector, faceEmbedder, db)
    }
}
