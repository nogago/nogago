package com.nogago.android.maps;

import java.util.List;

import com.nogago.android.maps.access.AccessibleToast;
import com.nogago.android.maps.activities.SettingsActivity;
import com.nogago.android.maps.plus.OsmandApplication;
import com.nogago.android.maps.plus.ProgressDialogImplementation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NogagoUtils {

	  
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) return false;
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) return false;
		return ni.isConnectedOrConnecting();
	}
	
	public static void reloadIndexes(final Activity activity){
		final ProgressDialog progressDlg = ProgressDialog.show(activity, activity.getString(R.string.loading_data), activity.getString(R.string.reading_indexes), true);
		final ProgressDialogImplementation impl = new ProgressDialogImplementation(progressDlg);
		impl.setRunnable("Indexing map data", new Runnable(){ //$NON-NLS-1$
			@Override
			public void run() {
				try {
					showWarnings(activity, ((OsmandApplication)activity.getApplicationContext()).getResourceManager().reloadIndexes(impl));
				} finally {
					if(progressDlg !=null){
						progressDlg.dismiss();
					}
				}
			}
		});
		impl.run();
	}
	
	private static void showWarnings(final Activity activity, List<String> warnings) {
		if (!warnings.isEmpty()) {
			final StringBuilder b = new StringBuilder();
			boolean f = true;
			for (String w : warnings) {
				if(f){
					f = false;
				} else {
					b.append('\n');
				}
				b.append(w);
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AccessibleToast.makeText(activity, b.toString(), Toast.LENGTH_LONG).show();
				}
			});
		}
	}

}
