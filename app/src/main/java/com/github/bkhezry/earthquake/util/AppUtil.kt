package com.github.bkhezry.earthquake.util

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.github.bkhezry.earthquake.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.roundToInt


class AppUtil {
    companion object {
        private lateinit var fastOutSlowIn: Interpolator
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

        fun vibrate() {
            val vibrator =
                MyApplication.applicationContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        10,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(10)
            }
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
            bitmap = createBitmapFromView(heroMarkerView)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        private fun createBitmapFromView(view: View): Bitmap {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(
                    dpToPx(90f), View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(

                    dpToPx(70f), View.MeasureSpec.EXACTLY
                )
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false

            return bitmap
        }

        fun getFastOutSlowInInterpolator(): Interpolator {
            fastOutSlowIn = AnimationUtils.loadInterpolator(
                MyApplication.applicationContext(),
                android.R.interpolator.fast_out_slow_in
            )
            return fastOutSlowIn
        }

        /**
         * Set the alpha component of `color` to be `alpha`.
         */
        @CheckResult
        @ColorInt
        fun modifyAlpha(
            @ColorInt color: Int,
            @IntRange(from = 0, to = 255) alpha: Int
        ): Int {
            return color and 0x00ffffff or (alpha shl 24)
        }

        /**
         * Determine if the navigation bar will be on the bottom of the screen, based on logic in
         * PhoneWindowManager.
         */
        fun isNavBarOnBottom(): Boolean {
            val res = MyApplication.applicationContext().resources
            val cfg = MyApplication.applicationContext().resources.configuration
            val dm = res.displayMetrics
            val canMove = dm.widthPixels != dm.heightPixels && cfg.smallestScreenWidthDp < 600
            return !canMove || dm.widthPixels < dm.heightPixels
        }
    }
}