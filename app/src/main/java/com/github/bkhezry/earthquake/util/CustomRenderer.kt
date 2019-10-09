package com.github.bkhezry.earthquake.util

import android.content.Context
import com.github.bkhezry.earthquake.model.Feature
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * The class for handle custom renderer for google map clustering
 *
 * @constructor
 */
class CustomRenderer(context: Context, map: GoogleMap, clusterManager: ClusterManager<Feature>) :
  DefaultClusterRenderer<Feature>(
    context,
    map, clusterManager
  ) {

  /**
   * Create custom marker for each cluster item with value of mag
   *
   * @param item Feature instance of feature
   * @param markerOptions MarkerOptions generated marker options
   */
  override fun onBeforeClusterItemRendered(item: Feature?, markerOptions: MarkerOptions?) {
    super.onBeforeClusterItemRendered(item, markerOptions)
    markerOptions!!.icon(
      AppUtil.setEarthquakeMarker(
        "M " + String.format(
          "%.1f",
          item!!.properties.mag
        )
      )
    )
  }
}