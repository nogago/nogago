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

import static com.google.android.apps.mytracks.util.Constants.TAG;

import com.google.android.apps.mytracks.services.RemoveTempFilesService;
import com.google.android.apps.mytracks.util.AnalyticsUtils;
import com.google.android.apps.mytracks.util.ApiAdapterFactory;
import com.google.android.apps.mytracks.util.EulaUtils;
import com.google.android.apps.mytracks.util.FileUtils;
import com.nogago.bb10.tracks.BuildConfig;

import android.app.Application;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * MyTracksApplication for keeping global state.
 * 
 * @author Jimmy Shih
 */
public class MyTracksApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    EulaUtils.increaseAppStart(this);
    Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
    if (BuildConfig.DEBUG) {
      ApiAdapterFactory.getApiAdapter().enableStrictMode();
    }
    AnalyticsUtils.sendPageViews(getApplicationContext(), "/appstart");
    Intent intent = new Intent(this, RemoveTempFilesService.class);
    startService(intent);
  }

  private class DefaultExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultHandler;

    public DefaultExceptionHandler() {
      defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
      if (FileUtils.isSdCardAvailable()) {
        File file = new File(FileUtils.buildExternalDirectoryPath("error.log"));
        try {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          PrintStream printStream = new PrintStream(out);
          ex.printStackTrace(printStream);
          StringBuilder msg = new StringBuilder();
          msg.append("Exception occured in thread " + thread.toString() + " : "). //$NON-NLS-1$ //$NON-NLS-2$
              append(DateFormat.format("MMMM dd, yyyy h:mm:ss", System.currentTimeMillis())).append("\n"). //$NON-NLS-1$//$NON-NLS-2$
              append(new String(out.toByteArray()));

          if (file.getParentFile().canWrite()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(msg.toString());
            writer.close();
          }
          defaultHandler.uncaughtException(thread, ex);
        } catch (Exception e) {
          Log.e(TAG, "Exception while handle other exception", e);
        }
      }
    }
  }
}
