package com.github.bkhezry.earthquake.service

import com.github.bkhezry.earthquake.model.EarthquakeHourResponse
import io.reactivex.Single
import retrofit2.http.GET

interface ApiService {

    @GET("all_hour.geojson")
    fun getHourlyEarthquake(): Single<EarthquakeHourResponse>
}