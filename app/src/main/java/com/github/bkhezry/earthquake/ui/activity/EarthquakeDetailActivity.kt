package com.github.bkhezry.earthquake.ui.activity

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.databinding.ActivityEarthquakeDetailBinding
import com.github.bkhezry.earthquake.model.Feature
import com.github.bkhezry.earthquake.util.AppUtil
import com.github.bkhezry.earthquake.util.Constants
import com.github.bkhezry.earthquake.util.ElasticDragDismissFrameLayout
import com.github.bkhezry.earthquake.util.SharedPreferencesUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

/**
 * The class for showing earthquake details in the Activity
 *
 * @property feature Feature data of earthquake
 * @property mMap GoogleMap instance of google map
 * @property grayScaleStyle MapStyleOptions theme of map
 * @property darkScaleStyle MapStyleOptions theme of map
 */
class EarthquakeDetailActivity : AppCompatActivity(), OnMapReadyCallback {

  private lateinit var feature: Feature
  private lateinit var mMap: GoogleMap
  private lateinit var grayScaleStyle: MapStyleOptions
  private lateinit var darkScaleStyle: MapStyleOptions
  private lateinit var viewBinding: ActivityEarthquakeDetailBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewBinding = ActivityEarthquakeDetailBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    initViews()
    setupMap()
    setupDismissLayout()
  }

  /**
   * Set variables value in the TextViews
   */
  private fun initViews() = with(viewBinding) {
    feature = intent.getParcelableExtra<Parcelable>(Constants.EXTRA_ITEM) as Feature
    val placeArray = feature.properties.place.split(",")
    val placeArraySplit = placeArray[0].split("of")
    val countryName = placeArray[placeArray.size - 1].trim()
    if (countryName.length == 2) {
      countryTextView.text = getString(R.string.usa_label).plus(countryName)
    } else {
      countryTextView.text = countryName
    }
    magTextView.text = String.format("%.1f", feature.properties.mag)
    if (placeArraySplit.size == 2) {
      cityTextView.text = placeArraySplit[1]
    }
    coordinatesTextView.text =
      String.format(
        "%.3f", feature.geometry.coordinates[1]
      ).plus("°, ").plus(
        String.format(
          "%.3f", feature.geometry.coordinates[0]
        )
      ).plus("°")
    depthTextView.text = String.format("%.1f", feature.geometry.coordinates[2]).plus(" km")
    val format = SimpleDateFormat("MMM d, yyyy hh:mm", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = feature.properties.time
    dateTimeTextView.text = format.format(calendar.time)
    distanceTextView.text = placeArray[0]
    detailButton.setOnClickListener { browseDetail() }
  }

  /**
   * Setup google map styles
   */
  private fun setupMap() {
    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    grayScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_grayscale)
    darkScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_dark)
    mapFragment.getMapAsync(this)
  }

  /**
   * Setup dismiss layout feature
   */

  private fun setupDismissLayout() {
    val dismissFrameLayout =
      findViewById<View>(R.id.draggable_frame) as ElasticDragDismissFrameLayout
    dismissFrameLayout.addListener(object : ElasticDragDismissFrameLayout.SystemChromeFader(this) {
      override fun onDragDismissed() {
        super.onDragDismissed()
        finishAfterTransition()
      }
    })
  }

  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    setupMapSettings()
  }

  /**
   * Set map settings
   * set coordinates of earthquake in the map
   * create marker of earthquake
   * set some settings in the settings of google map(like disable rotation & scroll gestures)
   */
  private fun setupMapSettings() {
    val latLng = LatLng(
      feature.geometry.coordinates[1],
      feature.geometry.coordinates[0]
    )
    if (SharedPreferencesUtil.getInstance(this).isDarkThemeEnabled) {
      mMap.setMapStyle(darkScaleStyle)
    } else {
      mMap.setMapStyle(grayScaleStyle)
    }
    mMap.moveCamera(
      CameraUpdateFactory.newLatLngZoom(
        latLng, 4f
      )
    )
    val markerOptions = MarkerOptions()
    markerOptions.icon(
      AppUtil.setEarthquakeMarker(
        String.format(
          "M %.1f",
          feature.properties.mag
        )
      )
    )
    markerOptions.position(latLng)
    mMap.addMarker(markerOptions)
    mMap.uiSettings.isMyLocationButtonEnabled = false
    mMap.uiSettings.isRotateGesturesEnabled = false
    mMap.uiSettings.isScrollGesturesEnabled = false
  }

  /**
   * Show earthquake details in the custom tab browser
   */
  private fun browseDetail() {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(this, Uri.parse(feature.properties.url))
  }
}
