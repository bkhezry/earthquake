package com.github.bkhezry.earthquake.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.model.EarthquakeHourResponse
import com.github.bkhezry.earthquake.service.ApiService
import com.github.bkhezry.earthquake.util.ApiClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var apiService: ApiService
    private val disposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        apiService = ApiClient.getClient()!!.create(ApiService::class.java)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        getHourlyEarthquake()
    }

    private fun getHourlyEarthquake() {
        val subscribe = apiService.getHourlyEarthquake()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleHourlyResponse, this::handleHourlyError)
        disposable.add(subscribe)

    }

    private fun handleHourlyResponse(earthquakeHourResponse: EarthquakeHourResponse) {
        Log.d("response:", earthquakeHourResponse.features.size.toString())
    }

    private fun handleHourlyError(error: Throwable) {
        Log.d("message:", error.localizedMessage)

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
