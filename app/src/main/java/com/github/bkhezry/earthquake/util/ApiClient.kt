package com.github.bkhezry.earthquake.util

import com.github.bkhezry.earthquake.util.ApiClient.Companion.REQUEST_TIMEOUT
import com.github.bkhezry.earthquake.util.ApiClient.Companion.okHttpClient
import com.github.bkhezry.earthquake.util.ApiClient.Companion.retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 *  A class for handle retrofit
 *  @property retrofit the instance of Retrofit class
 *  @property okHttpClient the instance of OkHttpClient class
 *  @property REQUEST_TIMEOUT the constant value for request timeout
 */
class ApiClient {
  companion object {
    private lateinit var retrofit: Retrofit
    private const val REQUEST_TIMEOUT = 10
    private lateinit var okHttpClient: OkHttpClient

    /**
     * Create instance of Retrofit class with custom settings
     *
     * @return Retrofit?
     */
    fun getClient(): Retrofit? {
      initOkHttp()
      retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
      return retrofit
    }

    /**
     * init OkHttpClient instance for using in the retrofit
     * add Interceptor for showing request in logcat
     */
    private fun initOkHttp() {
      val interceptor = HttpLoggingInterceptor()
      interceptor.level = HttpLoggingInterceptor.Level.BODY

      val httpClient = OkHttpClient().newBuilder()
        .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .addInterceptor(interceptor)

      okHttpClient = httpClient.build()
    }
  }

}
