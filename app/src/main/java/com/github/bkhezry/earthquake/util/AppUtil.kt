package com.github.bkhezry.earthquake.util

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.bkhezry.earthquake.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.roundToInt


class AppUtil {
    companion object {
        fun getActionBarHeight(context: Context): Int {
            var actionBarHeight = 0
            val tv = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    context.resources.displayMetrics
                )
            return actionBarHeight
        }

        fun dpToPx(dp: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp, Resources.getSystem().displayMetrics
            ).roundToInt()
        }

        fun setEarthquakeMarker(mag: String): BitmapDescriptor {
            val bitmap: Bitmap
            val heroMarkerView =
                (MyApplication.applicationContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.earthquake_marker,
                    null
                ) as LinearLayout
            val markerLine = heroMarkerView.findViewById<View>(R.id.marker_line) as ImageView
            val markerEnd = heroMarkerView.findViewById<View>(R.id.marker_end) as ImageView
            val markerEarthquake = heroMarkerView.findViewById<View>(R.id.mag_text_view) as TextView
            markerEarthquake.text = mag
            markerLine.setBackgroundColor(Color.BLACK)
            markerEnd.setImageResource(R.drawable.ic_action_end_marker)
            bitmap = createBitmapFromView(heroMarkerView, 90, 70)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        private fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
            if (width > 0 && height > 0) {
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        dpToPx(width.toFloat()), View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(

                        dpToPx(height.toFloat()), View.MeasureSpec.EXACTLY
                    )
                )
            }
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false

            return bitmap
        }
    }
}