package com.github.bkhezry.earthquake.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * The class for handle shared preferences values in static mode
 */
public class SharedPreferencesUtil {
  private static SharedPreferencesUtil instance = null;
  private SharedPreferences prefs;

  /**
   * Contractor
   *
   * @param context {@link Context}
   */
  private SharedPreferencesUtil(Context context) {
    prefs = PreferenceManager.getDefaultSharedPreferences(context);
  }

  /**
   * Get static value of haredPreferencesUtil class
   *
   * @param context {@link Context}
   * @return sharedPreferences instance of {@link SharedPreferencesUtil}
   */
  public static SharedPreferencesUtil getInstance(Context context) {

    if (instance == null) {
      instance = new SharedPreferencesUtil(context);
    }

    return instance;
  }

  /**
   * Get selected item of time filter
   *
   * @return int
   */
  public int getTimeSelected() {
    return prefs.getInt(Constants.TIME_SELECTED, 2);
  }

  /**
   * Set Selected item of time filter
   *
   * @param timeSelected int selected time filter
   */
  public void setTimeSelected(int timeSelected) {
    prefs.edit().putInt(Constants.TIME_SELECTED, timeSelected).apply();
  }

  /**
   * Get selected item of scale filter
   *
   * @return int
   */

  public int getScaleSelected() {
    return prefs.getInt(Constants.SCALE_SELECTED, 3);
  }

  /**
   * Set Selected item of scale filter
   *
   * @param scaleSelected int selected scale filter
   */
  public void setScaleSelected(int scaleSelected) {
    prefs.edit().putInt(Constants.SCALE_SELECTED, scaleSelected).apply();
  }

  /**
   * Return status of dark theme selection
   *
   * @return boolean if value of it is true, theme is dark
   */
  public boolean isDarkThemeEnabled() {
    return prefs.getBoolean(Constants.DARK_THEME, false);
  }

  /**
   * Set status of dark theme
   *
   * @param state boolean
   */
  public void setDarkThemeEnabled(boolean state) {
    prefs.edit().putBoolean(Constants.DARK_THEME, state).apply();
  }
}
