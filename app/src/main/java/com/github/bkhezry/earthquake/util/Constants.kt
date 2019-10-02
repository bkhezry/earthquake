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
        "10" to "all_day.geojson",
        "11" to "1.0_day.geojson",
        "12" to "2.5_day.geojson",
        "13" to "4.5_day.geojson",
        "20" to "all_week.geojson",
        "21" to "1.0_week.geojson",
        "22" to "2.5_week.geojson",
        "23" to "4.5_week.geojson",
        "30" to "all_month.geojson",
        "31" to "1.0_month.geojson",
        "32" to "2.5_month.geojson",
        "33" to "4.5_month.geojson"
      )
    var END_POINTS_NAME: HashMap<String, String> =
      hashMapOf(
        "00" to "Past Hour - All Earthquake",
        "01" to "Past Hour - M1.0+",
        "02" to "Past Hour - M2.5+",
        "03" to "Past Hour - M4.5+",
        "10" to "Past Day - All Earthquake",
        "11" to "Past Day - M1.0+",
        "12" to "Past Day - M2.5+",
        "13" to "Past Day - M4.5+",
        "20" to "Past 7 Days - All Earthquake",
        "21" to "Past 7 Days - M1.0+",
        "22" to "Past 7 Days - M2.5+",
        "23" to "Past 7 Days - M4.5+",
        "30" to "Past 30 Days - All Earthquake",
        "31" to "Past 30 Days - M1.0+",
        "32" to "Past 30 Days - M2.5+",
        "33" to "Past 30 Days - M4.5+"
      )
    const val TIME_SELECTED = "time-selected"
    const val SCALE_SELECTED = "scale-selected"
  }
}