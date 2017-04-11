/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.mytracks.settings;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.apps.mytracks.util.Constants;
import com.google.android.apps.mytracks.util.PreferencesUtils;
import com.nogago.bb10.tracks.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * An activity for accessing stats settings.
 * 
 * @author Jimmy Shih
 */
public class StatsSettingsActivity extends AbstractSettingsActivity {

  /*
   * Note that sharedPreferenceChangeListenr cannot be an anonymous inner class.
   * Anonymous inner class will get garbage collected.
   */
  private final OnSharedPreferenceChangeListener
      sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
          @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
          // Note that key can be null
          if (PreferencesUtils.getKey(StatsSettingsActivity.this, R.string.metric_units_key)
              .equals(key)) {
            updateUi();
          }
        }
      };

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    setContentView(R.layout.settings);
    ImageButton backButton = (ImageButton) findViewById(R.id.listBtnBarBack);
    if(backButton != null) backButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        StatsSettingsActivity.this.finish();
      }
    });
    addPreferencesFromResource(R.xml.stats_settings);

    getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateUi();
  }

  @SuppressWarnings("deprecation")
  private void updateUi() {
    CheckBoxPreference reportSpeedCheckBoxPreference = (CheckBoxPreference) findPreference(
        getString(R.string.report_speed_key));
    boolean metric = PreferencesUtils.getBoolean(
        this, R.string.metric_units_key, PreferencesUtils.METRIC_UNITS_DEFAULT);
    reportSpeedCheckBoxPreference.setSummaryOn(metric ? getString(R.string.description_speed_metric)
        : getString(R.string.description_speed_imperial));
    reportSpeedCheckBoxPreference.setSummaryOff(metric ? getString(R.string.description_pace_metric)
        : getString(R.string.description_pace_imperial));
  }

  @Override
  public void onStart() {
    super.onStart();
    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
  }

  @Override
  public void onStop() {
    super.onStop();
    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
  }
}
