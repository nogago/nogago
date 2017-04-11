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

package com.nogago.android.maps.views;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.R;
import com.nogago.android.maps.utils.EulaUtils;

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
public class ReviewDialogFragment extends DialogFragment {

  public static final String REVIEW_DIALOG_TAG = "reviewDialog";
  private static final String KEY_HAS_ACCEPTED = "hasAccepted";

  /**
   * Creates a new instance of {@link ReviewDialogFragment}.
   * 
   * @param hasAccepted true if the user has accepted the eula.
   */
  public static ReviewDialogFragment newInstance(boolean hasAccepted) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(KEY_HAS_ACCEPTED, hasAccepted);

    ReviewDialogFragment eulaDialogFragment = new ReviewDialogFragment();
    eulaDialogFragment.setArguments(bundle);
    return eulaDialogFragment;
  }

  private FragmentActivity activity;
  
  

  
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    activity = getActivity();
    
    boolean hasAccepted = getArguments().getBoolean(KEY_HAS_ACCEPTED);
    AlertDialog.Builder builder = new AlertDialog.Builder(activity)
        .setMessage(getEulaText())
        .setTitle(R.string.dlg_review_title);

    if (hasAccepted) {
      builder.setPositiveButton(android.R.string.ok, null);
    } else {
      builder.setNeutralButton(R.string.dlg_review_btn_feature,  new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // Send mail
        sendReviewMail();
        EulaUtils.setShowReview(activity);
      }
    }).setNegativeButton(R.string.dlg_review_btn_no, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          // Message user
          // Toast.makeText(activity, , Toast.LENGTH_LONG).show();
          AlertDialog.Builder builder2 = new AlertDialog.Builder(activity)
          .setMessage(R.string.dlg_review_btn_no_msg)
          .setTitle(R.string.dlg_review_title)
         .setPositiveButton(android.R.string.ok, null);
          builder2.create();
          builder2.show();
        }
      })
      .setPositiveButton(R.string.dlg_review_btn_review, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          EulaUtils.setShowReview(activity);
          Intent i = new Intent(Intent.ACTION_VIEW);
          i.setData(Uri.parse(Constants.IS_BLACKBERRY ? Constants.BB_MAPS_DOWNLOAD_URL : Constants.PLAY_MAPS_DOWNLOAD_URL ));
          startActivity(i);
        }
      });
    }
    return builder.create();
  }

  /**
   * Gets the EULA text.
   * 
   */
  private String getEulaText() {
    String tos = getString(R.string.dlg_review_body);
    return tos;
  }
  
  private void sendReviewMail() {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.putExtra(Intent.EXTRA_EMAIL, new String[] { Constants.SUPPORT_MAIL }); //$NON-NLS-1$
    intent.setType("vnd.android.cursor.dir/email"); //$NON-NLS-1$
    intent.putExtra(Intent.EXTRA_SUBJECT, "nogago Maps feature request and feedback"); //$NON-NLS-1$
    StringBuilder text = new StringBuilder();
    text.append(getString(R.string.dlg_review_btn_feature_msg));
    intent.putExtra(Intent.EXTRA_TEXT, text.toString());
    startActivity(Intent.createChooser(intent, getString(R.string.send_report)));
  }
}