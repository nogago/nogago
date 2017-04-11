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

// import com.google.android.apps.mytracks.fragments.MapFragment;
import com.google.android.apps.mytracks.fragments.StatsFragment;
import com.google.android.apps.mytracks.services.TrackRecordingServiceConnection;
import com.nogago.android.tracks.Constants;
import com.nogago.android.tracks.R;
import com.nogago.android.tracks.TrackDetailActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import java.util.HashMap;

/**
 * This is a helper class that implements a generic mechanism for associating
 * fragments with the tabs in a tab host. It relies on a trick. Normally a tab
 * host has a simple API for supplying a View or Intent that each tab will show.
 * This is not sufficient for switching between fragments. So instead we make
 * the content part of the tab host 0dp high (it is not shown) and the
 * TabManager supplies its own dummy view to show as the tab content. It listens
 * to changes in tabs, and takes care of switch to the correct fragment shown in
 * a separate content area whenever the selected tab changes.
 * <p>
 * Copied from the Fragment Tabs example in the API 4+ Support Demos.
 * 
 * @author Jimmy Shih
 */
public class TabManager implements TabHost.OnTabChangeListener {

  private final FragmentActivity fragmentActivity;
  private final TabHost tabHost;
  private final int containerId;
  private final HashMap<String, TabInfo> tabs = new HashMap<String, TabInfo>();
  private TrackRecordingServiceConnection trackRecordingServiceConnection;
  private TabInfo lastTabInfo;
  public boolean clicked = false;
  public boolean recording = false;

  /**
   * An object to hold a tab's info.
   * 
   * @author Jimmy Shih
   */
  private static final class TabInfo {

    private final String tag;
    private final Class<?> clss;
    private final Bundle bundle;
    private Fragment fragment;

    public TabInfo(String tag, Class<?> clss, Bundle bundle) {
      this.tag = tag;
      this.clss = clss;
      this.bundle = bundle;
    }
  }

  /**
   * A dummy {@link TabContentFactory} that creates an empty view to satisfy the
   * {@link TabHost} API.
   * 
   * @author Jimmy Shih
   */
  private static class DummyTabContentFactory implements TabContentFactory {

    private final Context context;

    public DummyTabContentFactory(Context context) {
      this.context = context;
    }

    @Override
    public View createTabContent(String tag) {
      View view = new View(context);
      view.setMinimumWidth(0);
      view.setMinimumHeight(0);
      return view;
    }
  }

  public TabManager(FragmentActivity fragmentActivity, TabHost tabHost, int containerId) {
    this.fragmentActivity = fragmentActivity;
    this.tabHost = tabHost;
    this.containerId = containerId;
    tabHost.setOnTabChangedListener(this);
    
  }
  public boolean isRecording() {
    if (TrackDetailActivity.RECORDING == true) {
      return true;
    }
    return false;
  }
  
  public void addTab(TabSpec tabSpec, Class<?> clss, Bundle bundle) {
    tabSpec.setContent(new DummyTabContentFactory(fragmentActivity));

    String tag = tabSpec.getTag();
    TabInfo tabInfo = new TabInfo(tag, clss, bundle);

    /*
     * Check to see if we already have a fragment for this tab, probably from a
     * previously saved state. If so, deactivate it, because our initial state
     * is that a tab isn't shown.
     */
    tabInfo.fragment = fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
    if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
      FragmentTransaction fragmentTransaction = fragmentActivity.getSupportFragmentManager()
          .beginTransaction();
      fragmentTransaction.detach(tabInfo.fragment);
      fragmentTransaction.commit();
    }
    tabs.put(tag, tabInfo);
    tabHost.addTab(tabSpec);
  }
  @Override
  public void onTabChanged(String tabId) {
    // Alert if nogago Maps isnt installed
    AlertDialog.Builder notInstalled = new AlertDialog.Builder(fragmentActivity);
    notInstalled.setMessage(R.string.maps_not_installed).setCancelable(false)
        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            Uri uri = Uri.parse(Constants.MAPS_DOWNLOAD_URL);
            Intent showUri = new Intent(Intent.ACTION_VIEW, uri);
            fragmentActivity.startActivity(showUri);
          }
        }).setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        });
    final AlertDialog alertnotInstalled = notInstalled.create();

    // Alert if nogago Maps is installed
    AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);
    builder.setMessage(R.string.wanna_start_maps).setCancelable(false)
        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            try {
              if (isRecording()) {
              String nogagoPackage = "com.nogago.android.maps";
              String mapActivity = ".activities.MapActivity";
              Intent intent = new Intent();
              intent.setComponent(new ComponentName(nogagoPackage, nogagoPackage + mapActivity));
              fragmentActivity.startActivity(intent);
              }
              else {
              String trackPackage ="com.nogago.android.tracks";
              String trackDetailActivity = ".TrackDetailActivity";
              Intent tda = new Intent();
              tda.setComponent(new ComponentName(trackPackage, trackPackage+trackDetailActivity));
              tda.putExtra("clicked", true);
              
              fragmentActivity.startActivity(tda);
              }
            } catch (NullPointerException e) {
              alertnotInstalled.show();
            }
          }
        }).setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        });
    AlertDialog alert = builder.create();

    if (tabId.compareTo("mapFragment")==0) {
      try {
        tabId = StatsFragment.STATS_FRAGMENT_TAG;
        // nogagoMaps aufrufen
        this.tabHost.setCurrentTab(1);
        try {
          if (isRecording()) {
          String nogagoPackage = "com.nogago.android.maps";
          String mapActivity = ".activities.MapActivity";
          Intent intent = new Intent();
          intent.setComponent(new ComponentName(nogagoPackage, nogagoPackage + mapActivity));
          fragmentActivity.startActivity(intent);
          }
          else {
          String trackPackage ="com.nogago.android.tracks";
          String trackDetailActivity = ".TrackDetailActivity";
          Intent tda = new Intent();
          tda.setComponent(new ComponentName(trackPackage, trackPackage+trackDetailActivity));
          tda.putExtra("clicked", true);
          
          fragmentActivity.startActivity(tda);
          }
        } catch (NullPointerException e) {
          alertnotInstalled.show();
        }
//        alert.show();
        // falls nicht installiert, fragen, ob installiert werden soll
      } catch (NullPointerException e) {

        alertnotInstalled.show();

      }
    }

    TabInfo newTabInfo = tabs.get(tabId);
    if (lastTabInfo != newTabInfo) {
      FragmentTransaction fragmentTransaction = fragmentActivity.getSupportFragmentManager()
          .beginTransaction();
      if (lastTabInfo != null) {
        if (lastTabInfo.fragment != null) {
          fragmentTransaction.detach(lastTabInfo.fragment);
        }
      }
      if (newTabInfo != null) {
        if (newTabInfo.fragment == null) {
          newTabInfo.fragment = Fragment.instantiate(fragmentActivity, newTabInfo.clss.getName(),
              newTabInfo.bundle);
          fragmentTransaction.add(containerId, newTabInfo.fragment, newTabInfo.tag);
        } else {
          fragmentTransaction.attach(newTabInfo.fragment);
        }
      }

      lastTabInfo = newTabInfo;
      fragmentTransaction.commit();
      fragmentActivity.getSupportFragmentManager().executePendingTransactions();
    }
  } public int isClicked() {
      if (clicked == true){
        return 1;
      } else {
        return 0;
      }
  }
}
