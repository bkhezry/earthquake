package com.github.bkhezry.earthquake.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
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

        fun dpToPx(dp: Int, r: Resources): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.displayMetrics
            ).roundToInt()
        }

    }
}