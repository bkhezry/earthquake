package com.github.bkhezry.earthquake.ui.activity

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.model.Feature
import com.github.bkhezry.earthquake.util.AppUtil
import com.github.bkhezry.earthquake.util.Constants
import com.github.bkhezry.earthquake.util.ElasticDragDismissFrameLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*


class EarthquakeDetailActivity : AppCompatActivity(), OnMapReadyCallback {

  private lateinit var feature: Feature
  private lateinit var mMap: GoogleMap
  private lateinit var grayScaleStyle: MapStyleOptions
  @BindView(R.id.country_text_view)
  lateinit var countryTextView: TextView
  @BindView(R.id.mag_text_view)
  lateinit var magTextView: TextView
  @BindView(R.id.city_text_view)
  lateinit var cityTextView: TextView
  @BindView(R.id.coordinates_text_view)
  lateinit var coordinatesTextView: TextView
  @BindView(R.id.depth_text_view)
  lateinit var depthTextView: TextView
  @BindView(R.id.date_time_text_view)
  lateinit var dateTimeTextView: TextView
  @BindView(R.id.distance_text_view)
  lateinit var distanceTextView: TextView


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_earthquake_detail)
    ButterKnife.bind(this)
    feature = intent.getParcelableExtra<Parcelable>(Constants.EXTRA_ITEM) as Feature
    val stringArray = feature.properties.place.split(",")
    val stringArray2 = stringArray[0].split("of")
    val country = stringArray[stringArray.size - 1].trim()
    if (country.length == 2) {
      countryTextView.text = getString(R.string.usa_label).plus(country)
    } else {
      countryTextView.text = country
    }
    magTextView.text = String.format("%.1f", feature.properties.mag)
    if (stringArray2.size == 2) {
      cityTextView.text = stringArray2[1]
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
    distanceTextView.text = stringArray[0]
    val dismissFrameLayout =
      findViewById<View>(R.id.draggable_frame) as ElasticDragDismissFrameLayout
    dismissFrameLayout.addListener(object :
      ElasticDragDismissFrameLayout.SystemChromeFader(this) {
      override fun onDragDismissed() {
        super.onDragDismissed()
        finishAfterTransition()
      }
    })
    val mapFragment = supportFragmentManager
      .findFragmentById(R.id.map) as SupportMapFragment
    grayScaleStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_grayscale)
    mapFragment.getMapAsync(this)
  }

  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    setupMapSettings()
  }

  private fun setupMapSettings() {
    val latLng = LatLng(
      feature.geometry.coordinates[1],
      feature.geometry.coordinates[0]
    )
    mMap.setMapStyle(grayScaleStyle)
    mMap.moveCamera(
      CameraUpdateFactory.newLatLngZoom(
        latLng, 4f
      )
    )
    val markerOptions = MarkerOptions()
    markerOptions.icon(AppUtil.setEarthquakeMarker(String.format("M %.1f", feature.properties.mag)))
    markerOptions.position(latLng)
    mMap.addMarker(markerOptions)
    mMap.uiSettings.isMyLocationButtonEnabled = false
    mMap.uiSettings.isRotateGesturesEnabled = false
    mMap.uiSettings.isScrollGesturesEnabled = false
  }
}
