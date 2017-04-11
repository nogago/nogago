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
import com.google.android.apps.mytracks.io.backup.BackupActivity;
import com.google.android.apps.mytracks.io.backup.RestoreChooserActivity;
import com.google.android.apps.mytracks.util.Constants;
import com.google.android.apps.mytracks.util.DialogUtils;
import com.google.android.apps.mytracks.util.FileUtils;
import com.google.android.apps.mytracks.util.IntentUtils;
import com.google.android.apps.mytracks.util.PreferencesUtils;
import com.nogago.bb10.tracks.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * An activity for accessing the backup settings.
 * 
 * @author Jimmy Shih
 */
public class BackupSettingsActivity extends AbstractSettingsActivity {

  private static final int DIALOG_CONFIRM_RESTORE_ID = 0;

  Preference backupPreference;
  Preference restorePreference;

  /*
   * Note that sharedPreferenceChangeListenr cannot be an anonymous inner class.
   * Anonymous inner class will get garbage collected.
   */
  private final OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
      // Note that key can be null
      if (PreferencesUtils.getKey(BackupSettingsActivity.this, R.string.recording_track_id_key)
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
    if (backButton != null)
      backButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          BackupSettingsActivity.this.finish();
        }
      });
    getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

    addPreferencesFromResource(R.xml.backup_settings);
    backupPreference = findPreference(getString(R.string.settings_backup_now_key));
    backupPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Intent intent = IntentUtils.newIntent(BackupSettingsActivity.this, BackupActivity.class);
        startActivity(intent);
        return true;
      }
    });
    restorePreference = findPreference(getString(R.string.settings_backup_restore_key));
    restorePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        showDialog(DIALOG_CONFIRM_RESTORE_ID);
        return true;
      }
    });
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    if (id != DIALOG_CONFIRM_RESTORE_ID) {
      return null;
    }
    return DialogUtils.createConfirmationDialog(this,
        R.string.settings_backup_restore_confirm_message, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String msg = String.format(getResources().getString(R.string.dlg_restore),
                Constants.IS_BLACKBERRY ? FileUtils.buildExternalDirectoryPath("backups")
                    .toString().replace("/mnt/sdcard", "/misc/android") : FileUtils
                    .buildExternalDirectoryPath("backups").toString());
            Builder builder = new AlertDialog.Builder(BackupSettingsActivity.this);
            builder.setMessage(msg).setNeutralButton(getString(android.R.string.cancel), null);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                Intent intent = IntentUtils.newIntent(BackupSettingsActivity.this,
                    RestoreChooserActivity.class);
                startActivity(intent);
              }
            });
            builder.show();

          }
        });

  }

  @Override
  protected void onResume() {
    super.onResume();
    updateUi();
  }

  /**
   * Updates the UI based on the recording state.
   */
  private void updateUi() {
    boolean isRecording = PreferencesUtils.getLong(this, R.string.recording_track_id_key) != PreferencesUtils.RECORDING_TRACK_ID_DEFAULT;
    backupPreference.setEnabled(!isRecording);
    restorePreference.setEnabled(!isRecording);
    backupPreference.setSummary(isRecording ? R.string.settings_not_while_recording
        : R.string.settings_backup_now_summary);
    restorePreference.setSummary(isRecording ? R.string.settings_not_while_recording
        : R.string.settings_backup_restore_summary);
  }

  @Override
  public void onStart() {
    super.onStart();
    EasyTracker.getInstance(this).activityStart(this); // Add this method.
  }

  @Override
  public void onStop() {
    super.onStop();
    EasyTracker.getInstance(this).activityStop(this); // Add this method.
  }

}
