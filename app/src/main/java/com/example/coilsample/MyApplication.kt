package com.example.coilsample

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.memory.MemoryCache
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class MyApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return createImageLoader(context)
    }


}
private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor()
        .apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }
}

private fun provideOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor = provideLoggingInterceptor()
): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()
@OptIn(ExperimentalCoilApi::class)
fun createImageLoader(context: PlatformContext) = ImageLoader.Builder(context)
    .components {
        add(
            OkHttpNetworkFetcherFactory(
                callFactory = {
                    provideOkHttpClient()
                },
                cacheStrategy = {
                    CacheControlCacheStrategy()
                }
            )
        )
    }.memoryCache {
        MemoryCache.Builder()
            .maxSizePercent(context, 0.25) // Use 25% of available memory
            .weakReferencesEnabled(true)
            .build()
    }
    .crossfade(300)
    .apply {
        if (BuildConfig.DEBUG) {
            logger(DebugLogger())
        }
    }.build()
