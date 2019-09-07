package com.github.bkhezry.earthquake.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.listener.CardClickListener
import com.github.bkhezry.earthquake.model.EarthquakeHourResponse
import com.github.bkhezry.earthquake.model.Feature
import com.github.bkhezry.earthquake.service.ApiService
import com.github.bkhezry.earthquake.util.ApiClient
import com.github.bkhezry.earthquake.util.AppUtil
import com.github.bkhezry.earthquake.util.LinearEdgeDecoration
import com.github.rubensousa.gravitysnaphelper.GravitySnapRecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.select.getSelectExtension
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    @BindView(R.id.bar)
    lateinit var bar: BottomAppBar
    @BindView(R.id.coordinator_layout)
    lateinit var coordinatorLayout: CoordinatorLayout
    @BindView(R.id.bound_fab)
    lateinit var boundFab: FloatingActionButton
    @BindView(R.id.recycler_view)
    lateinit var recyclerView: GravitySnapRecyclerView
    private lateinit var bottomDrawerBehavior: BottomSheetBehavior<View>
    private lateinit var mMap: GoogleMap
    private lateinit var grayScaleStyle: MapStyleOptions
    private lateinit var apiService: ApiService
    private val disposable = CompositeDisposable()
    private lateinit var mClusterManager: ClusterManager<Feature>
    private lateinit var mEarthquakeHourResponse: EarthquakeHourResponse
    private val itemAdapter = ItemAdapter<Feature>()
    private lateinit var fastAdapter: FastAdapter<Feature>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(bar)
        val params: (ViewGroup.MarginLayoutParams) =
            boundFab.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin =
            AppUtil.getActionBarHeight(this) + AppUtil.dpToPx(130, resources)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        grayScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_grayscale)
        mapFragment.getMapAsync(this)
        setupBottomDrawer()
        apiService = ApiClient.getClient()!!.create(ApiService::class.java)
        fastAdapter = FastAdapter.with(itemAdapter)
        fastAdapter.getSelectExtension().apply {
            isSelectable = true
            multiSelect = false
            selectOnLongClick = false
        }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(
            LinearEdgeDecoration(
                startPadding = recyclerView.resources.getDimensionPixelOffset(R.dimen.extra_padding),
                endPadding = 0,
                orientation = RecyclerView.HORIZONTAL
            )
        )
        recyclerView.adapter = fastAdapter
        fastAdapter.addEventHook(Feature.MaterialCardClickEvent(object : CardClickListener {
            override fun selected(feature: Feature, position: Int) {
                recyclerView.smoothScrollToPosition(position)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(feature.position, 9f))
            }
        }))
        fastAdapter.addEventHook(Feature.InfoFabClickEvent())
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
        setupMapSettings(googleMap)
        setupClusterManager()
        getHourlyEarthquake()
    }

    private fun setupClusterManager() {
        mClusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        mClusterManager.setOnClusterItemClickListener { feature ->
            handleClusterItemClick(feature)
        }
        mClusterManager.setOnClusterClickListener { cluster -> handleClusterClick(cluster) }
    }

    private fun setupMapSettings(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(grayScaleStyle)
        mMap.setPadding(
            0,
            0,
            0,
            AppUtil.getActionBarHeight(this) + AppUtil.dpToPx(130, resources)
        )
        // Add a marker in Sydney and move the camera
        val tehran = LatLng(35.6892, 51.3890)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tehran, 9f))
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isRotateGesturesEnabled = false
    }

    private fun handleClusterClick(cluster: Cluster<Feature>): Boolean {
        val features = arrayListOf<Feature>()
        for (item in cluster.items) {
            features.add(item)
        }
        boundBox(features)
        return true
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
        mEarthquakeHourResponse = earthquakeHourResponse
        mClusterManager.addItems(earthquakeHourResponse.features)
        boundBox(earthquakeHourResponse.features)
        itemAdapter.clear()
        for (item in earthquakeHourResponse.features) {
            item.isSelectable = true
            val feature = Feature(item.typeString, item.properties, item.geometry, item.id)
            itemAdapter.add(feature)
        }

    }

    private fun boundBox(features: List<Feature>) {
        val builder = LatLngBounds.builder()
        var count = 0
        for (item in features) {
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

    @OnClick(R.id.bound_fab)
    fun bound() {
        boundBox(mEarthquakeHourResponse.features)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
