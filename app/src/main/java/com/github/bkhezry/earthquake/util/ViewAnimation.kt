package com.github.bkhezry.earthquake.util

import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.animation.Animation
import android.view.animation.Transformation

object ViewAnimation {

  /**
   * Expand the view with animation
   *
   * @param v [View]
   */
  fun expand(v: View) {
    val matchParentMeasureSpec =
      View.MeasureSpec.makeMeasureSpec((v.parent as View).width, View.MeasureSpec.EXACTLY)
    val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    val targetHeight = v.measuredHeight

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    v.layoutParams.height = 1
    v.visibility = View.VISIBLE
    val animation = object : Animation() {
      override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        v.layoutParams.height = if (interpolatedTime == 1f)
          LayoutParams.WRAP_CONTENT
        else
          (targetHeight * interpolatedTime).toInt()
        v.requestLayout()
      }

      override fun willChangeBounds(): Boolean {
        return true
      }
    }

    // Expansion speed of 1dp/ms
    animation.duration =
      (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
    v.startAnimation(animation)
  }

  /**
   * Collapse the view with animation
   *
   * @param v [View]
   */
  fun collapse(v: View) {
    val initialHeight = v.measuredHeight

    val animation = object : Animation() {
      override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        if (interpolatedTime == 1f) {
          v.visibility = View.GONE
        } else {
          v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
          v.requestLayout()
        }
      }

      override fun willChangeBounds(): Boolean {
        return true
      }
    }

    animation.duration =
      (initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
    v.startAnimation(animation)
  }
}
