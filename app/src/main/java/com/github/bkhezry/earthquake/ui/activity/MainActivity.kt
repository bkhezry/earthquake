package com.github.bkhezry.earthquake.ui.activity

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
  @BindView(R.id.progress_bar)
  lateinit var progressBar: ProgressBar
  @BindView(R.id.filter_name_text_view)
  lateinit var filterNameTextView: TextView
  @BindView(R.id.record_count_text_view)
  lateinit var recordCountTextView: TextView
  @BindView(R.id.toggle_info_button)
  lateinit var toggleInfoButton: ImageButton
  @BindView(R.id.expand_layout)
  lateinit var expandLayout: LinearLayout
  private lateinit var mMap: GoogleMap
  private lateinit var grayScaleStyle: MapStyleOptions
  private lateinit var apiService: ApiService
  private val disposable = CompositeDisposable()
  private lateinit var mClusterManager: ClusterManager<Feature>
  private lateinit var mEarthquakeResponse: EarthquakeResponse
  private val itemAdapter = ItemAdapter<Feature>()
  private lateinit var fastAdapter: FastAdapter<Feature>
  private lateinit var customRenderer: CustomRenderer
  private lateinit var bottomSheetFilterBehavior: BottomSheetBehavior<View>
  private lateinit var bottomSheetAboutBehavior: BottomSheetBehavior<View>
  private lateinit var sharedPreferencesUtil: SharedPreferencesUtil

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)
    setSupportActionBar(bar)
    setUpBottomSheet()
    setupBottomDrawer()
    initVariables()
    setupRecyclerView()
    initMap()
  }

  private fun setupRecyclerView() {
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
  }

  private fun initVariables() {
    val params: (ViewGroup.MarginLayoutParams) =
      boundFab.layoutParams as ViewGroup.MarginLayoutParams
    params.bottomMargin =
      AppUtil.getActionBarHeight(this) + AppUtil.dpToPx(135f)
    apiService = ApiClient.getClient()!!.create(ApiService::class.java)
    sharedPreferencesUtil = SharedPreferencesUtil.getInstance(this)
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
    }
    filterNameTextView.text =
      Constants.END_POINTS_NAME[sharedPreferencesUtil.timeSelected.toString().plus(
        sharedPreferencesUtil.scaleSelected.toString()
      )]
  }

  private fun initMap() {
    val mapFragment = supportFragmentManager
      .findFragmentById(R.id.map) as SupportMapFragment
    grayScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_grayscale)
    mapFragment.getMapAsync(this)
  }

  private fun setupBottomDrawer() {
    bar.setNavigationOnClickListener {
      showAboutBottomSheet()
    }
    bar.setNavigationIcon(R.drawable.ic_menu_black_24dp)
  }

  private fun showAboutBottomSheet() {
    var versionName = ""
    try {
      versionName = packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: Exception) {
      // do nothing
    }
    setTextWithLinks(
      findViewById(R.id.text_application_info),
      getString(R.string.application_info_text, versionName)
    )
    setTextWithLinks(
      findViewById(R.id.text_developer_info),
      getString(R.string.developer_info_text)
    )
    setTextWithLinks(findViewById(R.id.text_design_api), getString(R.string.design_api_text))
    setTextWithLinks(findViewById(R.id.text_libraries), getString(R.string.libraries_text))
    setTextWithLinks(findViewById(R.id.text_license), getString(R.string.license_text))
    bottomSheetAboutBehavior.state = BottomSheetBehavior.STATE_EXPANDED
  }

  private fun setTextWithLinks(textView: TextView, htmlText: String) {
    AppUtil.setTextWithLinks(textView, AppUtil.fromHtml(htmlText))
  }

  private fun setUpBottomSheet() {
    val filterBottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet) as View
    val aboutBottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet_about) as View
    bottomSheetFilterBehavior = BottomSheetBehavior.from(filterBottomSheet)
    bottomSheetFilterBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    bottomSheetAboutBehavior = BottomSheetBehavior.from(aboutBottomSheet)
    bottomSheetAboutBehavior.state = BottomSheetBehavior.STATE_HIDDEN
  }

  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    setupMapSettings()
    setupClusterManager()
    getEarthquake()
    mMap.setOnMapClickListener {
      if (recyclerView.visibility == View.VISIBLE) {
        hideRecyclerView()
      } else {
        showRecyclerView()
      }
    }
  }

  private fun showRecyclerView() {
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

  private fun hideRecyclerView() {
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
    val zeroCoordinates = LatLng(0.0, 0.0)
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zeroCoordinates, 2f))
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
    progressBar.visibility = View.VISIBLE
    val endpoint = Constants.END_POINTS[
        sharedPreferencesUtil.timeSelected.toString().plus(
          sharedPreferencesUtil.scaleSelected.toString()
        )].toString()
    val subscribe = apiService.getEarthquakes(endpoint)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::handleResponse, this::handleError)
    disposable.add(subscribe)

  }

  private fun handleResponse(earthquakeResponse: EarthquakeResponse) {
    recordCountTextView.text = "#".plus(earthquakeResponse.metadata.count.toString())
    progressBar.visibility = View.GONE
    mEarthquakeResponse = earthquakeResponse
    mClusterManager.clearItems()
    mClusterManager.cluster()
    mClusterManager.addItems(earthquakeResponse.features)
    Handler().postDelayed({
      mClusterManager.cluster()
      boundBox(earthquakeResponse.features)
    }, 100)
    setRecyclerViewItems(earthquakeResponse.features)

  }

  private fun setRecyclerViewItems(features: List<Feature>) {
    itemAdapter.clear()
    val subList = getSubList(features, 0, 10)
    for (item in subList) {
      val feature = Feature(item.typeString, item.properties, item.geometry, item.id)
      itemAdapter.add(feature)
    }
  }

  private fun getSubList(features: List<Feature>, page: Int, count: Int): List<Feature> {
    val fromIndex = page * count
    val toIndex = fromIndex + count
    if (fromIndex == 0 && features.size < (toIndex - fromIndex)) {
      return features
    }
    return features.subList(fromIndex, toIndex)
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
    progressBar.visibility = View.GONE
    error.printStackTrace()
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
    }
    setFilter(timeChipSelected, scaleChipSelected)
  }

  private fun setFilter(timeChipSelected: Int, scaleChipSelected: Int) {
    sharedPreferencesUtil.scaleSelected = scaleChipSelected
    sharedPreferencesUtil.timeSelected = timeChipSelected
    filterNameTextView.text =
      Constants.END_POINTS_NAME[sharedPreferencesUtil.timeSelected.toString().plus(
        sharedPreferencesUtil.scaleSelected.toString()
      )]
    getEarthquake()
    hideBottomSheet()
  }

  private fun hideBottomSheet() {
    bottomSheetFilterBehavior.state = BottomSheetBehavior.STATE_HIDDEN
  }

  private fun expandBottomSheet() {
    bottomSheetFilterBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable.dispose()
  }

  override fun onBackPressed() {
    if (bottomSheetFilterBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
      hideBottomSheet()
    } else {
      super.onBackPressed()
    }

  }

  @OnClick(R.id.toggle_info_layout, R.id.toggle_info_button)
  fun toggleInfoLayout() {
    val show: Boolean = toggleArrow(toggleInfoButton)
    if (show) {
      ViewAnimation.expand(expandLayout)
    } else {
      ViewAnimation.collapse(expandLayout)
    }
  }


  private fun toggleArrow(view: View): Boolean {
    return if (view.rotation == 0f) {
      view.animate().setDuration(200).rotation(180f)
      true
    } else {
      view.animate().setDuration(200).rotation(0f)
      false
    }
  }
}
