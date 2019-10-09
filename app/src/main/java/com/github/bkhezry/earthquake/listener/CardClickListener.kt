package com.github.bkhezry.earthquake.listener

import com.github.bkhezry.earthquake.model.Feature

interface CardClickListener {
  /**
   * Return selected feature & position of it
   *
   * @param feature Feature the instance of feature
   * @param position Int the position ot feature in recycler view
   */
  fun selected(feature: Feature, position: Int)
}