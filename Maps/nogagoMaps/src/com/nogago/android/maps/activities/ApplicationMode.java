package com.nogago.android.maps.activities;

import com.nogago.android.maps.R;

import android.content.Context;

public enum ApplicationMode {
	/*
	 * DEFAULT("Default"), CAR("Car"), BICYCLE("Bicycle"), PEDESTRIAN("Pedestrian");
	 */

	//DEFAULT(R.string.app_mode_default), 
//	CAR(R.string.app_mode_hiking), 
	PEDESTRIAN(R.string.app_mode_pedestrian),
	BICYCLE(R.string.app_mode_bicycle) ;

	private final int key;

	ApplicationMode(int key) {
		this.key = key;
	}

	public String toHumanString(Context ctx) {
		return ctx.getResources().getString(key);
	}
	
	public static ApplicationMode fromString(String s){
		try {
			return ApplicationMode.valueOf(s.toUpperCase());
		} catch (IllegalArgumentException e) {
			return ApplicationMode.PEDESTRIAN;
		}
	}

}