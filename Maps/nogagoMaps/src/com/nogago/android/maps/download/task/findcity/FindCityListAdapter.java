package com.nogago.android.maps.download.task.findcity;


import java.util.ArrayList;
import java.util.List;

import com.nogago.android.maps.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FindCityListAdapter extends BaseAdapter {

	private final Context context;

	private List<CityCoordinate> poiSearchItems;

	/**
	 * Creates a new {@link FindCityByNameListAdapter} instance with a set of already known search results
	 * 
	 * @param context
	 *            the context
	 * @param toponyms
	 *            the entries
	 */
	public FindCityListAdapter(Context context, List<CityCoordinate> poiSearchItems) {
		this.context = context;
		this.poiSearchItems = poiSearchItems;
	}

	public FindCityListAdapter(Context context) {
		this.context = context;
		this.poiSearchItems = new ArrayList<CityCoordinate>();
	}

	/**
	 * Sets the entries
	 * 
	 * @param toponyms
	 *            the entries
	 */
	public void setPOISearchItems(List<CityCoordinate> pois) {
		this.poiSearchItems = pois;
		notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		if (this.poiSearchItems == null) {
			return 0;
		} else {
			return this.poiSearchItems.size();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		return this.poiSearchItems.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// if the view has not been created yet ...
		if (convertView == null) {
			// inflate the layout
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.find_entry, parent, false);
		
		}

		TextView entryView = (TextView) convertView.findViewById(R.id.findcity_entry_title);

		// get entry title
		CityCoordinate poiSearchItem = (CityCoordinate) getItem(position);
		String title = poiSearchItem.name;

		// set the entry title
		entryView.setText(title);

		return convertView;
	}
}
