package com.github.bkhezry.earthquake.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.model.EarthquakeHourResponse
import com.github.bkhezry.earthquake.model.Feature
import com.github.bkhezry.earthquake.service.ApiService
import com.github.bkhezry.earthquake.util.ApiClient
import com.github.bkhezry.earthquake.util.AppUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    @BindView(R.id.bar)
    lateinit var bar: BottomAppBar
    @BindView(R.id.coordinator_layout)
    lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var bottomDrawerBehavior: BottomSheetBehavior<View>
    private lateinit var mMap: GoogleMap
    private lateinit var grayScaleStyle: MapStyleOptions
    private lateinit var apiService: ApiService
    private val disposable = CompositeDisposable()
    private lateinit var mClusterManager: ClusterManager<Feature>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(bar)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        grayScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_grayscale)
        mapFragment.getMapAsync(this)
        setupBottomDrawer()
        apiService = ApiClient.getClient()!!.create(ApiService::class.java)
    }

    private fun setupBottomDrawer() {
        val bottomDrawer = coordinatorLayout.findViewById<View>(R.id.bottom_drawer)
        bottomDrawerBehavior = BottomSheetBehavior.from(bottomDrawer)
        bottomDrawerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bar.setNavigationOnClickListener {
            bottomDrawerBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
        bar.setNavigationIcon(R.drawable.ic_menu_black_24dp)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(grayScaleStyle)
        mMap.setPadding(
            0,
            AppUtil.getActionBarHeight(this) + AppUtil.dpToPx(30, resources),
            0,
            AppUtil.getActionBarHeight(this) + 5
        )
        // Add a marker in Sydney and move the camera
        val tehran = LatLng(35.6892, 51.3890)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tehran, 9f))
        mClusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        mClusterManager.setOnClusterItemClickListener { feature ->
            handleClusterItemClick(feature)
        }
        getHourlyEarthquake()
    }

    private fun handleClusterItemClick(feature: Feature): Boolean {
        Toast.makeText(this, feature.properties.title, Toast.LENGTH_LONG).show()
        return true
    }

    private fun getHourlyEarthquake() {
        val subscribe = apiService.getHourlyEarthquake()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleHourlyResponse, this::handleHourlyError)
        disposable.add(subscribe)

    }

    private fun handleHourlyResponse(earthquakeHourResponse: EarthquakeHourResponse) {
        val builder = LatLngBounds.builder()
        var count = 0
        for (item in earthquakeHourResponse.features) {
            mClusterManager.addItem(item)
            builder.include(item.position)
            count++
        }
        if (count > 1) {
            val bounds: LatLngBounds = builder.build()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }

    }

    private fun handleHourlyError(error: Throwable) {
        Log.d("message:", error.localizedMessage)

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
