package com.github.bkhezry.earthquake.ui.activity

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.listener.CardClickListener
import com.github.bkhezry.earthquake.model.EarthquakeResponse
import com.github.bkhezry.earthquake.model.Feature
import com.github.bkhezry.earthquake.service.ApiService
import com.github.bkhezry.earthquake.util.*
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
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.select.getSelectExtension
import com.mikepenz.itemanimators.AlphaInAnimator
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
  @BindView(R.id.chip_group_1)
  lateinit var chipGroup1: ChipGroup
  @BindView(R.id.chip_group_2)
  lateinit var chipGroup2: ChipGroup
  private lateinit var bottomDrawerBehavior: BottomSheetBehavior<View>
  private lateinit var mMap: GoogleMap
  private lateinit var grayScaleStyle: MapStyleOptions
  private lateinit var apiService: ApiService
  private val disposable = CompositeDisposable()
  private lateinit var mClusterManager: ClusterManager<Feature>
  private lateinit var mEarthquakeResponse: EarthquakeResponse
  private val itemAdapter = ItemAdapter<Feature>()
  private lateinit var fastAdapter: FastAdapter<Feature>
  private lateinit var customRenderer: CustomRenderer
  private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
  private lateinit var sharedPreferencesUtil: SharedPreferencesUtil

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)
    setSupportActionBar(bar)
    setUpBottomSheet()
    sharedPreferencesUtil = SharedPreferencesUtil.getInstance(this)
    val params: (ViewGroup.MarginLayoutParams) =
      boundFab.layoutParams as ViewGroup.MarginLayoutParams
    params.bottomMargin =
      AppUtil.getActionBarHeight(this) + AppUtil.dpToPx(135f)
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    val mapFragment = supportFragmentManager
      .findFragmentById(R.id.map) as SupportMapFragment
    grayScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_grayscale)
    setupBottomDrawer()
    apiService = ApiClient.getClient()!!.create(ApiService::class.java)
    fastAdapter = FastAdapter.with(itemAdapter)
    fastAdapter.getSelectExtension().apply {
      isSelectable = false
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
    recyclerView.itemAnimator = AlphaInAnimator()
    recyclerView.itemAnimator?.apply {
      addDuration = 500
      removeDuration = 500
    }

    recyclerView.adapter = fastAdapter
    fastAdapter.addEventHook(Feature.InfoFabClickEvent(object : CardClickListener {
      override fun selected(feature: Feature, position: Int) {
        recyclerView.smoothScrollToPosition(position)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(feature.position, 9f))
      }
    }))
    fastAdapter.onClickListener =
      { v: View?, _: IAdapter<Feature>, item: Feature, _: Int ->
        v?.let {
          val intent = Intent(this@MainActivity, EarthquakeDetailActivity::class.java)
          intent.putExtra(Constants.EXTRA_ITEM, item)
          val p1 = Pair.create<View, String>(v.findViewById(R.id.card_view), "card_view")
          val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@MainActivity,
            p1
          )
          startActivity(intent, options.toBundle())
        }
        true
      }
    when {
      sharedPreferencesUtil.timeSelected == 0 -> chipGroup1.check(R.id.time_chip_0)
      sharedPreferencesUtil.timeSelected == 1 -> chipGroup1.check(R.id.time_chip_1)
      sharedPreferencesUtil.timeSelected == 2 -> chipGroup1.check(R.id.time_chip_2)
      sharedPreferencesUtil.timeSelected == 3 -> chipGroup1.check(R.id.time_chip_3)
    }
    when {
      sharedPreferencesUtil.scaleSelected == 0 -> chipGroup2.check(R.id.scale_chip_0)
      sharedPreferencesUtil.scaleSelected == 1 -> chipGroup2.check(R.id.scale_chip_1)
      sharedPreferencesUtil.scaleSelected == 2 -> chipGroup2.check(R.id.scale_chip_2)
      sharedPreferencesUtil.scaleSelected == 3 -> chipGroup2.check(R.id.scale_chip_3)
      sharedPreferencesUtil.scaleSelected == 4 -> chipGroup2.check(R.id.scale_chip_4)
    }
    mapFragment.getMapAsync(this)
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

  private fun setUpBottomSheet() {
    val llBottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet) as View
    bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)
    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
  }

  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    setupMapSettings()
    setupClusterManager()
    getEarthquake()
    mMap.setOnMapClickListener {
      toggleRecyclerViewVisibility()
    }

  }

  private fun toggleRecyclerViewVisibility() {
    if (recyclerView.visibility == View.VISIBLE) {
      recyclerView.animate()
        .translationY(recyclerView.height.toFloat())
        .alpha(0.0f)
        .setDuration(200)
        .setListener(object : Animator.AnimatorListener {
          override fun onAnimationRepeat(animator: Animator?) {

          }

          override fun onAnimationCancel(animator: Animator?) {

          }

          override fun onAnimationStart(animator: Animator?) {

          }

          override fun onAnimationEnd(animator: Animator?) {
            mMap.setPadding(
              0,
              0,
              0,
              AppUtil.getActionBarHeight(this@MainActivity) + AppUtil.dpToPx(20f)
            )
            val params: (ViewGroup.MarginLayoutParams) =
              boundFab.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin =
              AppUtil.getActionBarHeight(this@MainActivity) + AppUtil.dpToPx(20f)
            recyclerView.clearAnimation()
            recyclerView.visibility = View.GONE
          }

        })
    } else {
      recyclerView.animate()
        .translationY(0f)
        .alpha(1.0f)
        .setDuration(200)
        .setListener(object : Animator.AnimatorListener {
          override fun onAnimationRepeat(animator: Animator?) {

          }

          override fun onAnimationCancel(animator: Animator?) {

          }

          override fun onAnimationStart(animator: Animator?) {
            mMap.setPadding(
              0,
              0,
              0,
              AppUtil.getActionBarHeight(this@MainActivity) + AppUtil.dpToPx(130f)
            )
            val params: (ViewGroup.MarginLayoutParams) =
              boundFab.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin =
              AppUtil.getActionBarHeight(this@MainActivity) + AppUtil.dpToPx(135f)
            recyclerView.clearAnimation()
            recyclerView.visibility = View.VISIBLE
          }

          override fun onAnimationEnd(animator: Animator?) {

          }

        })
    }
  }

  private fun setupClusterManager() {
    mClusterManager = ClusterManager(this, mMap)
    mMap.setOnCameraIdleListener(mClusterManager)
    mMap.setOnMarkerClickListener(mClusterManager)
    mClusterManager.setOnClusterItemClickListener { feature ->
      handleClusterItemClick(feature)
    }
    mClusterManager.setOnClusterClickListener { cluster -> handleClusterClick(cluster) }
    customRenderer = CustomRenderer(this@MainActivity, mMap, mClusterManager)
    mClusterManager.renderer = customRenderer
  }

  private fun setupMapSettings() {
    mMap.setMapStyle(grayScaleStyle)
    mMap.setPadding(
      0,
      0,
      0,
      AppUtil.getActionBarHeight(this) + AppUtil.dpToPx(130f)
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
    val intent = Intent(this@MainActivity, EarthquakeDetailActivity::class.java)
    intent.putExtra(Constants.EXTRA_ITEM, feature)
    startActivity(intent)
    return true
  }

  private fun getEarthquake() {
    val subscribe = apiService.getEarthquakes(
      Constants.END_POINTS[sharedPreferencesUtil.timeSelected.toString().plus(
        sharedPreferencesUtil.scaleSelected.toString()
      )].toString()
    )
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::handleResponse, this::handleError)
    disposable.add(subscribe)

  }

  private fun handleResponse(earthquakeResponse: EarthquakeResponse) {
    mEarthquakeResponse = earthquakeResponse
    mClusterManager.clearItems()
    mClusterManager.cluster()
    mClusterManager.addItems(earthquakeResponse.features)
    Handler().postDelayed({
      mClusterManager.cluster()
    }, 100)
    boundBox(earthquakeResponse.features)
    itemAdapter.clear()
    for (item in earthquakeResponse.features) {
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

  private fun handleError(error: Throwable) {
    Log.d("message:", error.localizedMessage)

  }

  @OnClick(R.id.bound_fab)
  fun bound() {
    Handler().postDelayed({
      boundBox(mEarthquakeResponse.features)
    }, 100)

  }

  @OnClick(R.id.filter_button)
  fun filter() {
    expandBottomSheet()
  }

  @OnClick(R.id.filter_done_button)
  fun filterDone() {
    //hideBottomSheet()
    var timeChipSelected = 0
    var scaleChipSelected = 0
    when {
      chipGroup1.checkedChipId == R.id.time_chip_0 -> timeChipSelected = 0
      chipGroup1.checkedChipId == R.id.time_chip_1 -> timeChipSelected = 1
      chipGroup1.checkedChipId == R.id.time_chip_2 -> timeChipSelected = 2
      chipGroup1.checkedChipId == R.id.time_chip_3 -> timeChipSelected = 3
    }
    when {
      chipGroup2.checkedChipId == R.id.scale_chip_0 -> scaleChipSelected = 0
      chipGroup2.checkedChipId == R.id.scale_chip_1 -> scaleChipSelected = 1
      chipGroup2.checkedChipId == R.id.scale_chip_2 -> scaleChipSelected = 2
      chipGroup2.checkedChipId == R.id.scale_chip_3 -> scaleChipSelected = 3
      chipGroup2.checkedChipId == R.id.scale_chip_4 -> scaleChipSelected = 4
    }
    setFilter(timeChipSelected, scaleChipSelected)
  }

  private fun setFilter(timeChipSelected: Int, scaleChipSelected: Int) {
    sharedPreferencesUtil.scaleSelected = scaleChipSelected
    sharedPreferencesUtil.timeSelected = timeChipSelected
    getEarthquake()
    hideBottomSheet()
  }

  private fun hideBottomSheet() {
    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
  }

  private fun expandBottomSheet() {
    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable.dispose()
  }

  override fun onBackPressed() {
    if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
      hideBottomSheet()
    } else {
      super.onBackPressed()
    }

  }
}
