package com.andrewkingmarshall.pexels.network.interceptors

import com.andrewkingmarshall.pexels.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthenticationInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
//            .addHeader("Authentication", )
            .build()

        return chain.proceed(request)
    }
}