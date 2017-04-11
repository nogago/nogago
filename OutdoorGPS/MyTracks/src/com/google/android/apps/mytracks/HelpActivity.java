/*
 * Copyright 2008 Google Inc.
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

package com.google.android.apps.mytracks;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.apps.mytracks.fragments.AboutDialogFragment;
import com.nogago.bb10.tracks.R;

import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * An activity that displays the help page.
 * 
 * @author Sandor Dornbush
 */
public class HelpActivity extends AbstractMyTracksActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Setup Action Bar
   
    Display display = getWindowManager().getDefaultDisplay();
    boolean devicesZ =display.getWidth() > 720 || display.getHeight() > 720;
    if(devicesZ) {
      // Disable the Keyboard help link
      findViewById(R.id.help_keyboard_q).setVisibility(View.GONE);
      findViewById(R.id.help_keyboard_a).setVisibility(View.GONE);
    } 
    try {
    findViewById(R.id.help_about).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        new AboutDialogFragment().show(
            getSupportFragmentManager(), AboutDialogFragment.ABOUT_DIALOG_TAG);
      }
    });
    findViewById(R.id.help_ok).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });
    } catch (NullPointerException ne) {
      // Layout does not have buttons
    }
    
  }
  
  /**
   * Returns true to hide the title. Be default, do not hide the title.
   */
  protected boolean hideTitle() {
    return true;
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.help;
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
