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
package com.google.android.apps.mytracks.io.sendtogoogle;

import com.google.android.apps.mytracks.settings.SettingsActivity;
import com.google.android.apps.mytracks.util.IntentUtils;
import com.google.android.apps.mytracks.util.PreferencesUtils;
import com.nogago.android.apps.tracks.io.SendNogagoActivity;
import com.nogago.bb10.tracks.R;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * A chooser to select an account.
 * 
 * @author Jimmy Shih
 */
public class AccountChooserActivity extends Activity {

  private static final String TAG = AccountChooserActivity.class.getSimpleName();

  private static final int DIALOG_NO_ACCOUNT_ID = 0;
  private static final int DIALOG_CHOOSER_ID = 1;

  /**
   * A callback after getting the permission to access a Google service.
   * 
   * @author Jimmy Shih
   */
  private interface PermissionCallback {

    /**
     * To be invoked when the permission is granted.
     */
    public void onSuccess();

    /**
     * To be invoked when the permission is not granted.
     */
    public void onFailure();
  }

  private SendRequest sendRequest;
  private Account[] accounts;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sendRequest = getIntent().getParcelableExtra(SendRequest.SEND_REQUEST_KEY);
    if (getUserName() != null && (getUserName().length() > 0)
        && getUserName().compareTo("username") != 0) {
      startNextActivity();
    } else {
      android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          Intent settings = new Intent(AccountChooserActivity.this, SettingsActivity.class);
          startActivity(settings);
        }
      };
      showAlertDialog(this, getString(R.string.wrong_credential),
          getString(R.string.error_username), listener);
    }

  }

  private void showAlertDialog(Context context, String title, String message,
      android.content.DialogInterface.OnClickListener listener) {
    Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(title);
    builder.setIcon(android.R.drawable.ic_dialog_info);
    builder.setMessage(message);
    builder.setNeutralButton(R.string.ok_button, listener);
    builder.show();
  }

  private String getUserName() {
    return PreferencesUtils.getString(this, R.string.user_name, "");
  }

  private String getPassword() {
    return PreferencesUtils.getString(this, R.string.user_password, "");
  }


  /**
   * Starts the next activity. If
   * <p>
   * sendMaps and newMap -> {@link SendMapsActivity}
   * <p>
   * sendMaps and !newMap -> {@link ChooseMapActivity}
   * <p>
   * !sendMaps && sendFusionTables -> {@link SendFusionTablesActivity}
   * <p>
   * !sendMaps && !sendFusionTables && sendDocs -> {@link SendDocsActivity}
   * <p>
   * !sendMaps && !sendFusionTables && !sendDocs -> {@link UploadResultActivity}
   */
  private void startNextActivity() {
    Class<?> next = SendNogagoActivity.class;

    Intent intent = IntentUtils.newIntent(this, next).putExtra(SendRequest.SEND_REQUEST_KEY,
        sendRequest);
    startActivity(intent);
    finish();
  }

}