package com.github.bkhezry.earthquake.util

class Constants {
  companion object {
    const val BASE_URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/"
    const val EXTRA_ITEM = "extra-item"
    var END_POINTS: HashMap<String, String> =
      hashMapOf(
        "00" to "all_hour.geojson",
        "01" to "1.0_hour.geojson",
        "02" to "2.5_hour.geojson",
        "03" to "4.5_hour.geojson",
        "04" to "significant_hour.geojson",
        "10" to "all_day.geojson",
        "11" to "1.0_day.geojson",
        "12" to "2.5_day.geojson",
        "13" to "4.5_day.geojson",
        "14" to "significant_day.geojson",
        "20" to "all_week.geojson",
        "21" to "1.0_week.geojson",
        "22" to "2.5_week.geojson",
        "23" to "4.5_week.geojson",
        "24" to "significant_week.geojson",
        "30" to "all_month.geojson",
        "31" to "1.0_month.geojson",
        "32" to "2.5_month.geojson",
        "33" to "4.5_month.geojson",
        "34" to "significant_month.geojson"
      )
    const val TIME_SELECTED = "time-selected"
    const val SCALE_SELECTED = "scale-selected"
  }
}