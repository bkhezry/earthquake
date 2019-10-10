package com.github.bkhezry.earthquake.ui.activity

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.blankj.utilcode.util.NetworkUtils
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

/**
 * Main activity class for showing data of earthquakes on the Google map
 *
 * @property bar BottomAppBar
 * @property coordinatorLayout CoordinatorLayout
 * @property boundFab FloatingActionButton
 * @property recyclerView GravitySnapRecyclerView
 * @property chipGroup1 ChipGroup
 * @property chipGroup2 ChipGroup
 * @property progressBar ProgressBar
 * @property filterNameTextView TextView
 * @property recordCountTextView TextView
 * @property toggleInfoButton ImageButton
 * @property expandLayout LinearLayout
 * @property nightModeSwitch SwitchCompat
 * @property mMap GoogleMap
 * @property grayScaleStyle MapStyleOptions the gray style of map
 * @property darkScaleStyle MapStyleOptions the dark style of map
 * @property apiService ApiService
 * @property disposable CompositeDisposable
 * @property mClusterManager ClusterManager<Feature> the cluster manager for showing marker cluster
 * @property mEarthquakeResponse EarthquakeResponse the response of server for earthquakes
 * @property itemAdapter ItemAdapter<Feature> the ItemAdapter for handle feature items
 * @property fastAdapter FastAdapter<Feature> the FastAdapter for using in the recycler view
 * @property customRenderer CustomRenderer the custom renderer instance for handle markers of cluster
 * @property bottomSheetFilterBehavior BottomSheetBehavior<View> the bottom sheet behavior
 * @property bottomSheetAboutBehavior BottomSheetBehavior<View> the bottom sheet behavior
 * @property sharedPreferencesUtil SharedPreferencesUtil the instance of shared preferences
 */

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
  @BindView(R.id.night_mode_switch)
  lateinit var nightModeSwitch: SwitchCompat
  private lateinit var mMap: GoogleMap
  private lateinit var grayScaleStyle: MapStyleOptions
  private lateinit var darkScaleStyle: MapStyleOptions
  private lateinit var apiService: ApiService
  private val disposable = CompositeDisposable()
  private lateinit var mClusterManager: ClusterManager<Feature>
  private var mEarthquakeResponse: EarthquakeResponse? = null
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
    setupAboutBottomSheet()
    setupRecyclerView()
    initMap()
  }

  /**
   * Setup recycler view with the adapter of it
   * handle recycler view animation
   * set recycler view item click listener
   */
  private fun setupRecyclerView() {
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

  /**
   * Setup variables value
   * Create instance of variables
   */
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

  /**
   * Setup map styles
   */
  private fun initMap() {
    val mapFragment = supportFragmentManager
      .findFragmentById(R.id.map) as SupportMapFragment
    grayScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_grayscale)
    darkScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_dark)
    mapFragment.getMapAsync(this)
  }

  /**
   * setup bottom drawer behavior
   */
  private fun setupBottomDrawer() {
    bar.setNavigationOnClickListener {
      bottomSheetAboutBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
    bar.setNavigationIcon(R.drawable.ic_menu_black_24dp)
  }

  /**
   * Setup about bottom sheet values.
   * set values to TextViews. set theme of app
   */
  private fun setupAboutBottomSheet() {
    nightModeSwitch.isChecked = sharedPreferencesUtil.isDarkThemeEnabled
    nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
      sharedPreferencesUtil.isDarkThemeEnabled = isChecked
      Handler().postDelayed({
        if (isChecked) {
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
      }, 300)
    }
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
  }

  private fun setTextWithLinks(textView: TextView, htmlText: String) {
    AppUtil.setTextWithLinks(textView, AppUtil.fromHtml(htmlText))
  }

  /**
   * Setup bottom sheet behavior of about & filter sheet
   */
  private fun setUpBottomSheet() {
    val filterBottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet) as View
    val aboutBottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet_about) as View
    bottomSheetFilterBehavior = BottomSheetBehavior.from(filterBottomSheet)
    bottomSheetFilterBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    bottomSheetAboutBehavior = BottomSheetBehavior.from(aboutBottomSheet)
    bottomSheetAboutBehavior.state = BottomSheetBehavior.STATE_HIDDEN
  }

  /**
   * Handle google map event listener
   *
   * @param googleMap GoogleMap
   */
  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    setupMapSettings()
    setupClusterManager()
    getEarthquake()
    mMap.setOnMapClickListener {
      if (bottomSheetAboutBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
        bottomSheetAboutBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        return@setOnMapClickListener
      }
      if (bottomSheetFilterBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
        bottomSheetFilterBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        return@setOnMapClickListener
      }
      if (recyclerView.visibility == View.VISIBLE) {
        hideRecyclerView()
      } else {
        showRecyclerView()
      }
    }
  }

  /**
   * Show recycler view with animation
   */
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

  /**
   * Hide recycler view with animation
   */
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

  /**
   * Setup cluster manager
   * add cluster & cluster item click listener
   */
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

  /**
   * Set google map settings
   * default coordinates & zoom with some google map settings(like disable rotation gestures)
   */
  private fun setupMapSettings() {
    if (sharedPreferencesUtil.isDarkThemeEnabled) {
      mMap.setMapStyle(darkScaleStyle)
    } else {
      mMap.setMapStyle(grayScaleStyle)
    }
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

  /**
   * Handle cluster click event
   * bound box google map to the items of cluster
   *
   * @param cluster Cluster<Feature> instance of Cluster
   * @return Boolean
   */
  private fun handleClusterClick(cluster: Cluster<Feature>): Boolean {
    val features = arrayListOf<Feature>()
    for (item in cluster.items) {
      features.add(item)
    }
    boundBox(features)
    return true
  }

  /**
   * Handle cluster item click event
   * showing EarthquakeDetailActivity with data of this item
   *
   * @param feature Feature instance
   * @return Boolean
   */
  private fun handleClusterItemClick(feature: Feature): Boolean {
    val intent = Intent(this@MainActivity, EarthquakeDetailActivity::class.java)
    intent.putExtra(Constants.EXTRA_ITEM, feature)
    startActivity(intent)
    return true
  }

  /**
   * Get earthquakes data from server
   */
  private fun getEarthquake() {
    if (NetworkUtils.isConnected()) {
      requestEarthquakes()
    } else {
      Toast.makeText(this, "No internet access!", Toast.LENGTH_LONG).show()
    }
  }

  private fun requestEarthquakes() {
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

  /**
   * Handle response of server
   *
   * add items of feature to recycler view
   * add items of feature to cluster manager
   * bound box map for showing data on the google map
   *
   * @param earthquakeResponse EarthquakeResponse
   */
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

  /**
   * Showing 10 items of features in the recycler view
   *
   * @param features List<Feature> list of features
   */
  private fun setRecyclerViewItems(features: List<Feature>) {
    itemAdapter.clear()
    val subList = getSubList(features, 0, 10)
    for (item in subList) {
      val feature = Feature(item.typeString, item.properties, item.geometry, item.id)
      itemAdapter.add(feature)
    }
  }

  /**
   * Select sub list of features item
   *
   * @param features List<Feature> list of features
   * @param page Int page number
   * @param count Int count of features
   * @return List<Feature> sub list of features
   */
  private fun getSubList(features: List<Feature>, page: Int, count: Int): List<Feature> {
    val fromIndex = page * count
    val toIndex = fromIndex + count
    if (fromIndex == 0 && features.size < (toIndex - fromIndex)) {
      return features
    }
    return features.subList(fromIndex, toIndex)
  }

  /**
   * Bound box map to showing data on the google maps
   *
   * @param features List<Feature> list of features
   */
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

  /**
   * Handle error during get earthquakes from server
   *
   * @param error Throwable
   */
  private fun handleError(error: Throwable) {
    progressBar.visibility = View.GONE
    error.printStackTrace()
  }

  /**
   * bound map base on the all features data
   */
  @OnClick(R.id.bound_fab)
  fun bound() {
    Handler().postDelayed({
      mEarthquakeResponse?.let {
        boundBox(it.features)
      }
    }, 100)

  }

  /**
   * Show the filter bottom sheet
   */
  @OnClick(R.id.filter_button)
  fun filter() {
    expandBottomSheet()
  }

  /**
   * set selected time & scale as filter for get earthquakes data
   */
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

  /**
   * Store selected filters in the SharedPreferencesUtil
   *
   * @param timeChipSelected Int selected time filter
   * @param scaleChipSelected Int selected scale filter
   */
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
    bottomSheetFilterBehavior.state = BottomSheetBehavior.STATE_EXPANDED
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable.dispose()
  }

  /**
   * Handle back pressed button
   * if any bottom sheet is showing, before close app, the will be hide
   */
  override fun onBackPressed() {
    if (bottomSheetFilterBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
      hideBottomSheet()
      return
    }
    if (bottomSheetAboutBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
      bottomSheetAboutBehavior.state = BottomSheetBehavior.STATE_HIDDEN
      return
    }
    super.onBackPressed()

  }

  /**
   * Toggle info layout of about bottom sheet
   */
  @OnClick(R.id.toggle_info_layout, R.id.toggle_info_button)
  fun toggleInfoLayout() {
    val show: Boolean = toggleArrow(toggleInfoButton)
    if (show) {
      ViewAnimation.expand(expandLayout)
    } else {
      ViewAnimation.collapse(expandLayout)
    }
  }

  /**
   * Rotate icon after click on it
   *
   * @param view View of arrow icon
   * @return Boolean
   */
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
