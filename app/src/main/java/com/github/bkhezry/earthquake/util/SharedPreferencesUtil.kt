package com.github.bkhezry.earthquake.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


/**
 * The class for handle shared preferences values in static mode
 */
class SharedPreferencesUtil
/**
 * Contractor
 *
 * @param context [Context]
 */
private constructor(context: Context) {
  private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  /**
   * Get selected item of time filter
   *
   * @return int
   */
  /**
   * Set Selected item of time filter
   *
   * @param timeSelected int selected time filter
   */
  var timeSelected: Int
    get() = prefs.getInt(Constants.TIME_SELECTED, 2)
    set(timeSelected) = prefs.edit().putInt(Constants.TIME_SELECTED, timeSelected).apply()

  /**
   * Get selected item of scale filter
   *
   * @return int
   */

  /**
   * Set Selected item of scale filter
   *
   * @param scaleSelected int selected scale filter
   */
  var scaleSelected: Int
    get() = prefs.getInt(Constants.SCALE_SELECTED, 3)
    set(scaleSelected) = prefs.edit().putInt(Constants.SCALE_SELECTED, scaleSelected).apply()

  /**
   * Return status of dark theme selection
   *
   * @return boolean if value of it is true, theme is dark
   */
  /**
   * Set status of dark theme
   *
   * @param state boolean
   */
  var isDarkThemeEnabled: Boolean
    get() = prefs.getBoolean(Constants.DARK_THEME, false)
    set(state) = prefs.edit().putBoolean(Constants.DARK_THEME, state).apply()

  companion object {
    private var instance: SharedPreferencesUtil? = null

    /**
     * Get static value of haredPreferencesUtil class
     *
     * @param context [Context]
     * @return sharedPreferences instance of [SharedPreferencesUtil]
     */
    fun getInstance(context: Context): SharedPreferencesUtil {

      if (instance == null) {
        instance = SharedPreferencesUtil(context)
      }

      return instance as SharedPreferencesUtil
    }
  }
}