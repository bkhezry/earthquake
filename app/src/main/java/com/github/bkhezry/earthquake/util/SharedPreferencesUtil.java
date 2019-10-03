package com.github.bkhezry.earthquake.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesUtil {
  private static SharedPreferencesUtil instance = null;
  private SharedPreferences prefs;

  private SharedPreferencesUtil(Context context) {
    prefs = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public static SharedPreferencesUtil getInstance(Context context) {

    if (instance == null) {
      instance = new SharedPreferencesUtil(context);
    }

    return instance;
  }

  public int getTimeSelected() {
    return prefs.getInt(Constants.TIME_SELECTED, 1);
  }

  public void setTimeSelected(int timeSelected) {
    prefs.edit().putInt(Constants.TIME_SELECTED, timeSelected).apply();
  }

  public int getScaleSelected() {
    return prefs.getInt(Constants.SCALE_SELECTED, 1);
  }

  public void setScaleSelected(int scaleSelected) {
    prefs.edit().putInt(Constants.SCALE_SELECTED, scaleSelected).apply();
  }

  public boolean isDarkThemeEnabled() {
    return prefs.getBoolean(Constants.DARK_THEME, false);
  }

  public void setDarkThemeEnabled(boolean state) {
    prefs.edit().putBoolean(Constants.DARK_THEME, state).apply();
  }
}
