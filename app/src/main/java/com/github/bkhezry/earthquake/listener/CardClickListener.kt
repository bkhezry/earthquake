package com.github.bkhezry.earthquake.listener

import com.github.bkhezry.earthquake.model.Feature

interface CardClickListener {
  fun selected(feature: Feature, position: Int)
}