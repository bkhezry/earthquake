package com.github.bkhezry.earthquake.model


import com.google.gson.annotations.SerializedName

data class Metadata(
  @SerializedName("generated")
  val generated: Long,
  @SerializedName("url")
  val url: String,
  @SerializedName("title")
  val title: String,
  @SerializedName("status")
  val status: Int,
  @SerializedName("api")
  val api: String,
  @SerializedName("count")
  val count: Int
)