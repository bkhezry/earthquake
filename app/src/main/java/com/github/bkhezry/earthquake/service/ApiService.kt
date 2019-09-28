package com.github.bkhezry.earthquake.service

import com.github.bkhezry.earthquake.model.EarthquakeResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

  @GET("{endpoint}")
  fun getEarthquakes(@Path("endpoint") get: String): Single<EarthquakeResponse>
}