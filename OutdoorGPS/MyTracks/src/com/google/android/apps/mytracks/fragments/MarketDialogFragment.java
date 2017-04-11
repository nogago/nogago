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

import com.google.android.apps.mytracks.util.Constants;
import com.nogago.bb10.tracks.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

/**
 * A DialogFragment to show EULA.
 * 
 * @author Jimmy Shih
 */
public class MarketDialogFragment extends DialogFragment {

  public static final String MARKET_DIALOG_TAG = "marketDialog";
  private static final String KEY_HAS_ACCEPTED = "hasAccepted";

  /**
   * Creates a new instance of {@link MarketDialogFragment}.
   * 
   * @param hasAccepted true if the user has accepted the eula.
   */
  public static MarketDialogFragment newInstance(boolean hasAccepted) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(KEY_HAS_ACCEPTED, hasAccepted);

    MarketDialogFragment eulaDialogFragment = new MarketDialogFragment();
    eulaDialogFragment.setArguments(bundle);
    return eulaDialogFragment;
  }

  private FragmentActivity activity;
  
  

  
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    activity = getActivity();
    
    boolean hasAccepted = getArguments().getBoolean(KEY_HAS_ACCEPTED);
    AlertDialog.Builder builder = new AlertDialog.Builder(activity)
        .setMessage(R.string.dlg_market_body)
        .setTitle(R.string.dlg_market_title);

      builder.setNegativeButton(R.string.dlg_market_btn_no, null)
      .setPositiveButton(R.string.dlg_market_btn_yes, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          Intent i = new Intent(Intent.ACTION_VIEW);
          i.setData(Uri.parse(Constants.IS_BLACKBERRY ? Constants.BB_VENDOR_URL : Constants.PLAY_VENDOR_URL ));
          startActivity(i);
         }
      });
    return builder.create();
  }


  
}