/*
 * Copyright 2010 Google Inc.
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


import android.app.Activity;

/**
 * A set of methods that may be implemented differently depending on the Android
 * API level.
 *
 * @author Raphael Volz
 */
public interface ApiAdapter {

  /**
   * Hides the title. If the platform supports the action bar, do nothing.
   * Ideally, with the action bar, we would like to collapse the navigation tabs
   * into the action bar. However, collapsing is not supported by the
   * compatibility library.
   * <p>
   * Due to changes in API level 11.
   * 
   * @param activity the activity
   */
  public void hideTitle(Activity activity);

  /**
   * Configures the action bar with the Home button as an Up button. If the
   * platform doesn't support the action bar, do nothing.
   * <p>
   * Due to changes in API level 11.
   *
   * @param activity the activity
   */
  public void configureActionBarHomeAsUp(Activity activity);

 
  /**
   * Hides the action bar (on newer SDK
   */
  public void hideActionBar(Activity activity);
}
