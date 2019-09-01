package com.github.bkhezry.earthquake.model


import com.google.gson.annotations.SerializedName

data class Properties(
    @SerializedName("mag")
    val mag: Double,
    @SerializedName("place")
    val place: String,
    @SerializedName("time")
    val time: Long,
    @SerializedName("updated")
    val updated: Long,
    @SerializedName("tz")
    val tz: Int,
    @SerializedName("url")
    val url: String,
    @SerializedName("detail")
    val detail: String,
    @SerializedName("felt")
    val felt: Any,
    @SerializedName("cdi")
    val cdi: Any,
    @SerializedName("mmi")
    val mmi: Any,
    @SerializedName("alert")
    val alert: Any,
    @SerializedName("status")
    val status: String,
    @SerializedName("tsunami")
    val tsunami: Int,
    @SerializedName("sig")
    val sig: Int,
    @SerializedName("net")
    val net: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("ids")
    val ids: String,
    @SerializedName("sources")
    val sources: String,
    @SerializedName("types")
    val types: String,
    @SerializedName("nst")
    val nst: Int,
    @SerializedName("dmin")
    val dmin: Double,
    @SerializedName("rms")
    val rms: Double,
    @SerializedName("gap")
    val gap: Int,
    @SerializedName("magType")
    val magType: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("title")
    val title: String
)