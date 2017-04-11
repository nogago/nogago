package com.nogago.android.maps.plus;

import java.io.File;

import net.osmand.IProgress;

public class PoiRegion {
	double leftLongitude;
	double rightLongitude;
	double topLatitude;
	double bottomLatitude;
	AmenityIndexRepositoryOdb db;
	
	public double getLeftLongitude() {
		return leftLongitude;
	}
	public void setLeftLongitude(double leftLongitude) {
		this.leftLongitude = leftLongitude;
	}
	public double getRightLongitude() {
		return rightLongitude;
	}
	public void setRightLongitude(double rightLongitude) {
		this.rightLongitude = rightLongitude;
	}
	public double getTopLatitude() {
		return topLatitude;
	}
	public void setTopLatitude(double topLatitude) {
		this.topLatitude = topLatitude;
	}
	public double getBottomLatitude() {
		return bottomLatitude;
	}
	public void setBottomLatitude(double bottomLatitude) {
		this.bottomLatitude = bottomLatitude;
	}
	
	public PoiRegion(File file){
		db = new AmenityIndexRepositoryOdb();
		db.initialize(IProgress.EMPTY_PROGRESS, file);
		bottomLatitude = db.dataBottomLatitude;
		leftLongitude = db.dataLeftLongitude;
		rightLongitude = db.dataRightLongitude;
		topLatitude = db.dataTopLatitude;
		db.close();
	}
}
