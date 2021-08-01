package com.andrewkingmarshall.pexels.network.interceptors

import android.content.Context
import com.andrewkingmarshall.pexels.R
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorInterceptor(private val context: Context) : Interceptor {

    @Throws(NetworkException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val response: Response = try {
            chain.proceed(chain.request())

        } catch (notConnectedToNetworkException: UnknownHostException) {
            val noNetworkDetectedException = UnknownHostException(context.getString(R.string.no_network_detected))
            Timber.w(noNetworkDetectedException, "UnknownHostException. Check network connection")
            throw noNetworkDetectedException

        } catch (timeoutException: SocketTimeoutException) {
            val networkTimeoutException =
                SocketTimeoutException(context.getString(R.string.timeout))
            Timber.w(networkTimeoutException, "NetworkTimeoutException. Network times out")
            throw networkTimeoutException
        }

        if (!response.isSuccessful) {
            var errorDescription = response.message
            val httpErrorCode = response.code
            if (httpErrorCode == 500) {
                errorDescription = "Internal Server Error (500)"
            }
            val networkException =
                NetworkException(errorDescription, httpErrorCode)
            Timber.w(networkException, "Network Error occurred: %s", networkException.toString())
            throw networkException
        }
        return response
    }

}

// Note: This has to be IOException, otherwise Coroutines will crash:
//      https://stackoverflow.com/questions/58697459/handle-exceptions-thrown-by-a-custom-okhttp-interceptor-in-kotlin-coroutines
class NetworkException(val errorMessage: String, val httpErrorCode: Int = -1) : IOException(errorMessage)