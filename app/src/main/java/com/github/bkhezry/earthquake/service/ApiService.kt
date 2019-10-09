package com.github.bkhezry.earthquake.service

import com.github.bkhezry.earthquake.model.EarthquakeResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

  /**
   * Get earthquakes data from server base on the endpoint value
   *
   * @param get String value of endpoint
   * @return Single<EarthquakeResponse> instance of response from server as EarthquakeResponse
   */
  @GET("{endpoint}")
  fun getEarthquakes(@Path("endpoint") get: String): Single<EarthquakeResponse>
}