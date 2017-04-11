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

package com.google.android.apps.mytracks.fragments;

import com.google.android.apps.mytracks.settings.MapSettingsActivity;
import com.google.android.apps.mytracks.util.Constants;
import com.google.android.apps.mytracks.util.IntentUtils;
import com.nogago.bb10.tracks.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

/**
 * A DialogFragment to install nogago Maps.
 *
 * @author Raphael Volz
 */
public class InstallMapsDialogFragment extends DialogFragment {

  public static final String INSTALL_MAPS_DIALOG_TAG = "installMapsDialog";

  private FragmentActivity activity;
  
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    activity = getActivity();
    return new AlertDialog.Builder(activity)
        .setMessage(R.string.track_detail_install_maps_message)
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent intent = IntentUtils.newIntent(activity, MapSettingsActivity.class);
            startActivity(intent);
          }
        })
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent().setData(Uri.parse(Constants.IS_BLACKBERRY ? Constants.BB_MAPS_DOWNLOAD_URL : Constants.PLAY_MAPS_DOWNLOAD_URL));
            try {
              startActivity(intent);
            } catch (ActivityNotFoundException e) {
              Toast.makeText(
                  activity, R.string.track_detail_install_maps_error, Toast.LENGTH_LONG)
                  .show();
            }
          }
        })
        .create();
  }
  
  /**
   * Creates a new instance of {@link ReviewDialogFragment}.
   * 
   * @param hasAccepted true if the user has accepted the eula.
   */
  public static InstallMapsDialogFragment newInstance() {
    return new InstallMapsDialogFragment();
    }
}