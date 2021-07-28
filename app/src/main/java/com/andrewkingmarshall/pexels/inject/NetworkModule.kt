package com.andrewkingmarshall.pexels.inject

import android.content.Context
import com.andrewkingmarshall.pexels.BuildConfig
import com.andrewkingmarshall.pexels.R
import com.andrewkingmarshall.pexels.network.interceptors.AuthenticationInterceptor
import com.andrewkingmarshall.pexels.network.interceptors.ErrorInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideHttpClient(@ApplicationContext context: Context): OkHttpClient {

        val httpLoggingInterceptor = HttpLoggingInterceptor()

        // Set Log Level
        if (BuildConfig.DEBUG) {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }

        // Set up the HTTP Client
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(ErrorInterceptor(context))
            .addInterceptor(AuthenticationInterceptor())
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(@ApplicationContext context: Context, httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.pexel_base_endpoint))
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

}