package com.github.bkhezry.earthquake.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Geometry(
  @SerializedName("type")
  val type: String,
  @SerializedName("coordinates")
  val coordinates: List<Double>
) : Parcelable