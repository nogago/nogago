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
import android.view.Window;

/**
 * API level 7 specific implementation of the {@link ApiAdapter}.
 *
 * @author Bartlomiej Niechwiej
 */
public class Api7Adapter implements ApiAdapter {

  @Override
  public void hideTitle(Activity activity) {
    activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
  }

  @Override
  public void configureActionBarHomeAsUp(Activity activity) {
    // Do nothing
  }


  @Override
  public void hideActionBar(Activity activity) {
    // TODO Use Android Support Library hiding
    
  }
}
