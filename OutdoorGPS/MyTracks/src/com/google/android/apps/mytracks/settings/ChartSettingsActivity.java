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
import com.google.android.apps.mytracks.util.PreferencesUtils;
import com.nogago.bb10.tracks.R;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * An activity for accessing chart settings.
 * 
 * @author Jimmy Shih
 */
public class ChartSettingsActivity extends AbstractSettingsActivity {

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    setContentView(R.layout.settings);
    addPreferencesFromResource(R.xml.chart_settings);

    ImageButton backButton = (ImageButton) findViewById(R.id.listBtnBarBack);
    if(backButton != null)
    backButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        ChartSettingsActivity.this.finish();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateUi();
  }

  @SuppressWarnings("deprecation")
  private void updateUi() {
    CheckBoxPreference speedCheckBoxPreference = (CheckBoxPreference) findPreference(
        getString(R.string.chart_show_speed_key));
    boolean reportSpeed = PreferencesUtils.getBoolean(
        this, R.string.report_speed_key, PreferencesUtils.REPORT_SPEED_DEFAULT);
    speedCheckBoxPreference.setTitle(reportSpeed ? R.string.stats_speed
        : R.string.stats_pace);
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
