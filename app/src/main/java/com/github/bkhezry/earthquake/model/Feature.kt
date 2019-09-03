package com.github.bkhezry.earthquake.model


import android.view.View
import com.github.bkhezry.earthquake.R
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

data class Feature(
    @SerializedName("type")
    val typeString: String,
    @SerializedName("properties")
    val properties: Properties,
    @SerializedName("geometry")
    val geometry: Geometry,
    @SerializedName("id")
    val id: String
) : ClusterItem, AbstractItem<Feature.ViewHolder>() {
    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override val type: Int
        get() = R.id.fastadapter_sample_item_id

    /** defines the layout which will be used for this item in the list  */
    override val layoutRes: Int
        get() = R.layout.feature_item


    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return LatLng(geometry.coordinates[1], geometry.coordinates[1])
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<Feature>(view) {


        override fun bindView(item: Feature, payloads: MutableList<Any>) {

        }

        override fun unbindView(item: Feature) {

        }
    }

}