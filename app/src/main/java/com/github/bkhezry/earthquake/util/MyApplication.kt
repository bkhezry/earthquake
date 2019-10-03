package com.github.bkhezry.earthquake.util

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MyApplication : Application() {

  init {
    instance = this
  }

  companion object {
    private var instance: MyApplication? = null

    fun applicationContext(): Context {
      return instance!!.applicationContext
    }
  }

  override fun onCreate() {
    super.onCreate()
    if (SharedPreferencesUtil.getInstance(this).isDarkThemeEnabled) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
    val context: Context = applicationContext()
  }
}