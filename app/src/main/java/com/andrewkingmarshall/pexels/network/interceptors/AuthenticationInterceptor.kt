package com.andrewkingmarshall.pexels.network.interceptors

import com.andrewkingmarshall.pexels.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the Pexel Api Key to all network call headers.
 */
class AuthenticationInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            // Note: You can just paste your API Key here (or add it in local.properties) if you are building locally
            .addHeader("Authorization", BuildConfig.PEXEL_API_KEY)
            .build()

        return chain.proceed(request)
    }
}