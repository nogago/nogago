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
package com.nogago.android.tracks;

import com.google.android.apps.mytracks.io.docs.SendDocsAsyncTask;
import com.google.android.apps.mytracks.io.sendtogoogle.AbstractSendActivity;
import com.google.android.apps.mytracks.io.sendtogoogle.AbstractSendAsyncTask;

/**
 * An activity to send a track to Google Docs.
 *
 * @author Jimmy Shih
 */
public class SendDocsActivity extends AbstractSendActivity {

  @Override
  protected AbstractSendAsyncTask createAsyncTask() {
    return new SendDocsAsyncTask(this, sendRequest.getTrackId(), sendRequest.getAccount());
  }

  @Override
  protected String getServiceName() {
    return getString(R.string.send_google_docs);
  }

  @Override
  protected void startNextActivity(boolean success, boolean isCancel) {
    sendRequest.setDocsSuccess(success);
    /*
    Intent intent = IntentUtils.newIntent(this, UploadResultActivity.class)
        .putExtra(SendRequest.SEND_REQUEST_KEY, sendRequest);
    startActivity(intent);
    */
    finish();
  }
}
