package com.nogago.android.maps.activities;


import com.bidforfix.andorid.BidForFixActivity;
import com.bidforfix.andorid.BidForFixHelper;
import com.nogago.android.maps.plus.OsmandApplication;

public class OsmandBidForFixActivity extends BidForFixActivity {

	@Override
	public BidForFixHelper getBidForFixHelper() {
		return ((OsmandApplication) getApplication()).getBidForFix();
	}
}
