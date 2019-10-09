package com.github.bkhezry.earthquake.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Html
import android.text.Spannable
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.util.AppUtil.Companion.fastOutSlowIn
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.roundToInt

/**
 * The class for handle some useful functions
 * @property fastOutSlowIn instance of Interpolator
 */

class AppUtil {
  companion object {
    private lateinit var fastOutSlowIn: Interpolator

    /**
     * Calculate height of actionbar
     *
     * @param context Context instance of application context
     * @return Int height of action bar
     */
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

    /**
     * Calculate pixel of dp value
     *
     * @param dp Float value of dp
     * @return Int pixel of dp value
     */
    fun dpToPx(dp: Float): Int {
      return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp, Resources.getSystem().displayMetrics
      ).roundToInt()
    }

    /**
     * Vibrate device for 10 ms
     */
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

    /**
     * Create custom marker base on the mag value and
     * custom design of earthquake_marker layout
     *
     * @param mag String value of earthquake mag
     * @return BitmapDescriptor instance of BitmapDescriptor
     */
    fun setEarthquakeMarker(mag: String): BitmapDescriptor {
      val bitmap: Bitmap
      val heroMarkerView =
        (MyApplication.applicationContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
          R.layout.earthquake_marker,
          null
        ) as LinearLayout
      val cardView = heroMarkerView.findViewById(R.id.marker_card_view) as CardView
      val whiteColorStateList =
        ContextCompat.getColorStateList(
          MyApplication.applicationContext(),
          android.R.color.white
        ) as ColorStateList
      val markerEarthquake = heroMarkerView.findViewById<View>(R.id.mag_text_view) as TextView
      markerEarthquake.text = mag
      if (SharedPreferencesUtil.getInstance(MyApplication.applicationContext()).isDarkThemeEnabled) {
        cardView.backgroundTintList = whiteColorStateList
        markerEarthquake.setTextColor(Color.BLACK)
      }
      bitmap = createBitmapFromView(heroMarkerView)
      return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Generate bitmap instance from view with custom measures
     *
     * @param view View instance of view
     * @return Bitmap generated bitmap from view
     */
    private fun createBitmapFromView(view: View): Bitmap {
      view.measure(
        View.MeasureSpec.makeMeasureSpec(
          dpToPx(60f), View.MeasureSpec.EXACTLY
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

    /**
     * Create fast out show in animation
     *
     * @return Interpolator instance of animation interpolator
     */
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

    /**
     * Set string on text view with support link in it
     *
     * @param textView TextView instance of text view
     * @param html CharSequence? String of text view
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setTextWithLinks(textView: TextView, html: CharSequence?) {
      textView.text = html
      textView.setOnTouchListener(View.OnTouchListener { v, event ->
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
          var x = event.x.toInt()
          var y = event.y.toInt()

          val widget = v as TextView
          x -= widget.totalPaddingLeft
          y -= widget.totalPaddingTop

          x += widget.scrollX
          y += widget.scrollY

          val layout = widget.layout
          val line = layout.getLineForVertical(y)
          val off = layout.getOffsetForHorizontal(line, x.toFloat())

          val link = Spannable.Factory.getInstance()
            .newSpannable(widget.text)
            .getSpans(off, off, ClickableSpan::class.java)

          if (link.isNotEmpty()) {
            if (action == MotionEvent.ACTION_UP) {
              link[0].onClick(widget)
            }
            return@OnTouchListener true
          }
        }
        false
      })
    }

    /**
     * Create html from string
     *
     * @param htmlText String
     * @return CharSequence?
     */
    fun fromHtml(htmlText: String): CharSequence? {
      if (TextUtils.isEmpty(htmlText)) {
        return null
      }
      val spanned: CharSequence = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
      } else {
        Html.fromHtml(htmlText)
      }
      return spanned.trim()
    }

  }
}