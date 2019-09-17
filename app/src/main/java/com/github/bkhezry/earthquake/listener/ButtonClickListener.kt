package com.github.bkhezry.earthquake.listener

import android.view.View
import com.github.bkhezry.earthquake.model.Feature

interface ButtonClickListener {
    fun selected(
        feature: Feature,
        position: Int,
        view: View
    )
}