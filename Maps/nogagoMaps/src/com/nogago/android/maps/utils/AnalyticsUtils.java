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

package com.nogago.android.maps.utils;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.nogago.android.maps.Constants;

import android.content.Context;

/**
 * Utitlites for sending pageviews to Google Analytics.
 * 
 * @author Jimmy Shih
 */
public class AnalyticsUtils {

  private static EasyTracker tracker;

  private AnalyticsUtils() {}

  /**
   * Sends a page view.
   * 
   * @param context the context
   * @param page the page
   */
  public static void sendPageViews(Context context, String page) {
    if (Constants.isOnline(context)) {
      tracker = EasyTracker.getInstance(context);
      tracker.set(Fields.SCREEN_NAME, page);
      tracker.send(MapBuilder.createAppView().build());
    }
  }

}
