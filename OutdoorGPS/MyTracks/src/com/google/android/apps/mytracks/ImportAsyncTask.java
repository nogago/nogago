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

package com.google.android.apps.mytracks;

import com.google.android.apps.mytracks.io.file.GpxImporter;
import com.google.android.apps.mytracks.util.FileUtils;
import com.google.android.apps.mytracks.util.PreferencesUtils;
import com.google.android.apps.mytracks.util.SystemUtils;
import com.nogago.android.apps.tracks.content.MyTracksProviderUtils;
import com.nogago.bb10.tracks.R;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * AsyncTask to import GPX files from the SD card.
 *
 * @author Jimmy Shih
 */
public class ImportAsyncTask extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = ImportAsyncTask.class.getSimpleName();

    private ImportActivity importActivity;
    private final boolean importAll;
    private final String name;
    private final Uri fileName;
    private final MyTracksProviderUtils myTracksProviderUtils;
    private WakeLock wakeLock;

    // true if the AsyncTask result is success
    private boolean success;

    // true if the AsyncTask has completed
    private boolean completed;

    // number of files successfully imported
    private int successCount;

    // number of files to import
    private int totalCount;

    // the last successfully imported track id
    private long trackId;

    /**
     * Creates an AsyncTask.
     *
     * @param importActivity the activity currently associated with this AsyncTask
     * @param importAll      true to import all GPX files
     * @param path           path to import GPX files
     */
    public ImportAsyncTask(ImportActivity importActivity, boolean importAll, String name,
                           final Uri file) {
        this.importActivity = importActivity;
        this.importAll = importAll;
        this.name = name;
        this.fileName = file;

        myTracksProviderUtils = MyTracksProviderUtils.Factory.get(importActivity);

        // Get the wake lock if not recording or paused
        if (PreferencesUtils.getLong(importActivity, R.string.recording_track_id_key) == PreferencesUtils.RECORDING_TRACK_ID_DEFAULT
                || PreferencesUtils.getBoolean(importActivity, R.string.recording_track_paused_key,
                PreferencesUtils.RECORDING_TRACK_PAUSED_DEFAULT)) {
            wakeLock = SystemUtils.acquireWakeLock(importActivity, wakeLock);
        }

        success = false;
        completed = false;
        successCount = 0;
        totalCount = 0;
        trackId = -1L;
    }

    /**
     * Sets the current {@link ImportActivity} associated with this AyncTask.
     *
     * @param importActivity the current {@link ImportActivity}, can be null
     */
    public void setActivity(ImportActivity importActivity) {
        this.importActivity = importActivity;
        if (completed && importActivity != null) {
            importActivity.onAsyncTaskCompleted(success, successCount, totalCount, trackId);
        }
    }

    @Override
    protected void onPreExecute() {
        if (importActivity != null) {
            importActivity.showProgressDialog();
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (!FileUtils.isSdCardAvailable()) {
                return false;
            }

            List<File> files = new ArrayList<File>();
            File file1;
            if (fileName != null) {
                file1 = new File(fileName.getPath());
            } else {
                file1 = new File(FileUtils.buildExternalDirectoryPath("gpx"));
            }

            if (importAll) {
                File[] candidates = file1.listFiles();
                if (candidates != null) {
                    for (File candidate : candidates) {
                        if (!candidate.isDirectory() && candidate.getName().endsWith(".gpx")) {
                            files.add(candidate);
                        }
                    }
                }
            } else
                files.add(file1);


            totalCount = files.size();
            if (totalCount == 0) {
                return true;
            }
            if (totalCount == 1) {
                // handle as Uri
                importUri(fileName);
                successCount++;
            } else {
                for (int i = 0; i < totalCount; i++) {
                    if (isCancelled()) {
                        // If cancelled, return true to show the number of files imported
                        return true;
                    }
                    File file = files.get(i);
                    if (importFile(file)) {
                        successCount++;
                    }
                    publishProgress(i + 1, totalCount);
                }
            }
            return true;
        } finally {
            // Release the wake lock if obtained
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (importActivity != null) {
            importActivity.setProgressDialogValue(values[0], values[1]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        success = result;
        completed = true;
        if (importActivity != null) {
            importActivity.onAsyncTaskCompleted(success, successCount, totalCount, trackId);
        }
    }

    /**
     * Imports a GPX file.
     *
     * @param file the file
     */
    private boolean importFile(final File file) {
        try {
            int minRecordingDistance = PreferencesUtils.getInt(importActivity,
                    R.string.min_recording_distance_key, PreferencesUtils.MIN_RECORDING_DISTANCE_DEFAULT);
            long trackIds[] = GpxImporter.importGPXFile(new FileInputStream(file), myTracksProviderUtils,
                    minRecordingDistance);
            int length = trackIds.length;
            if (length > 0) {
                trackId = trackIds[length - 1];
            }
            return true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "file: " + file.getAbsolutePath(), e);
            return false;
        } catch (ParserConfigurationException e) {
            Log.d(TAG, "file: " + file.getAbsolutePath(), e);
            return false;
        } catch (SAXException e) {
            Log.d(TAG, "file: " + file.getAbsolutePath(), e);
            return false;
        } catch (IOException e) {
            Log.d(TAG, "file: " + file.getAbsolutePath(), e);
            return false;
        }
    }

    private boolean importUri(final Uri uri) {
        InputStream is = null;

        try {

            final ParcelFileDescriptor pFD = importActivity.getContentResolver().openFileDescriptor(uri,
                    "r");

            if (pFD != null) {

                is = new FileInputStream(pFD.getFileDescriptor());

                try {
                    int minRecordingDistance = PreferencesUtils.getInt(importActivity,
                            R.string.min_recording_distance_key, PreferencesUtils.MIN_RECORDING_DISTANCE_DEFAULT);
                    long trackIds[] = GpxImporter.importGPXFile(is, myTracksProviderUtils,
                            minRecordingDistance);
                    int length = trackIds.length;
                    if (length > 0) {
                        trackId = trackIds[length - 1];
                    }
                    return true;
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "file: " + uri.toString(), e);
                    return false;
                } catch (ParserConfigurationException e) {
                    Log.d(TAG, "file: " + uri.toString(), e);
                    return false;
                } catch (SAXException e) {
                    Log.d(TAG, "file: " + uri.toString(), e);
                    return false;
                } catch (IOException e) {
                    Log.d(TAG, "file: " + uri.toString(), e);
                    return false;
                }

            }

        } catch (FileNotFoundException e) {

            //

        } finally {

            if (is != null)
                try {

                    is.close();

                } catch (IOException ignore) {

                }

        }

        return false;
    }
}

