package com.nogago.android.maps.activities;

import com.nogago.android.maps.R;
import com.nogago.android.maps.plus.OsmandApplication;

import android.app.ExpandableListActivity;
import android.widget.ExpandableListView;

public abstract class OsmandExpandableListActivity extends
		ExpandableListActivity {

	@Override
	protected void onResume() {
		super.onResume();
		
		ExpandableListView view = getExpandableListView();
		view.setCacheColorHint(getResources().getColor(R.color.activity_background));
		view.setDivider(getResources().getDrawable(R.drawable.tab_text_separator));
	}

	protected OsmandApplication getMyApplication() {
		return (OsmandApplication)getApplication();
	}
}
