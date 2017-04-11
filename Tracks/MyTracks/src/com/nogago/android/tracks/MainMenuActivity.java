package com.nogago.android.tracks;

import com.google.android.apps.mytracks.fragments.CheckUnitsDialogFragment;
import com.google.android.apps.mytracks.services.ITrackRecordingService;
import com.google.android.apps.mytracks.services.TrackRecordingServiceConnection;
import com.google.android.apps.mytracks.util.EulaUtils;
import com.google.android.apps.mytracks.util.IntentUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

public class MainMenuActivity extends FragmentActivity {

	private static final String FIRST_TIME_APP_RUN = "FIRST_TIME_APP_RUN"; //$NON-NLS-1$
	private static final String VECTOR_INDEXES_CHECK = "VECTOR_INDEXES_CHECK"; //$NON-NLS-1$
	private static final String TIPS_SHOW = "TIPS_SHOW"; //$NON-NLS-1$
	private static final String VERSION_INSTALLED = "VERSION_INSTALLED"; //$NON-NLS-1$
//	private static final String EXCEPTION_FILE_SIZE = ResourceManager.APP_DIR + "exception.log"; //$NON-NLS-1$
	
	private static final String CONTRIBUTION_VERSION_FLAG = "CONTRIBUTION_VERSION_FLAG";
	
	public static final int APP_EXIT_CODE = 4;
	public static final String APP_EXIT_KEY = "APP_EXIT_KEY";
	
	private TrackRecordingServiceConnection trackRecordingServiceConnection;
	private static final String TAG = TrackListActivity.class.getSimpleName();
	private long recordingTrackId;
//	private boolean startNewRecording = false;
	
	
  // Callback when the trackRecordingServiceConnection binding changes.
  private final Runnable bindChangedCallback = new Runnable() {
//    @Override
    public void run() {
//       if (!startNewRecording) {
//         return;
//       }

      ITrackRecordingService service = trackRecordingServiceConnection.getServiceIfBound();
      if (service == null) {
        Log.d(TAG, "service not available to start a new recording");
        return;
      }
      try {
        recordingTrackId = service.startNewTrack();
//        startNewRecording = true;
        Intent intent = IntentUtils.newIntent(MainMenuActivity.this, TrackDetailActivity.class)
            .putExtra(TrackDetailActivity.EXTRA_TRACK_ID, recordingTrackId);
        startActivity(intent);
        Toast.makeText(MainMenuActivity.this, R.string.track_record_notification, Toast.LENGTH_LONG).show();
       
        

      } catch (Exception e) {
        Toast.makeText(MainMenuActivity.this, R.string.track_list_record_error, Toast.LENGTH_LONG)
            .show();
        Log.e(TAG, "Unable to start a new recording.", e);
      }
    }
  };
		
	/*
	public void checkPreviousRunsForExceptions(boolean firstTime) {
		long size = getPreferences(MODE_WORLD_READABLE).getLong(EXCEPTION_FILE_SIZE, 0);
		final File file = MyTracksApplication.getSettings().extendOsmandPath(OsmandApplication.EXCEPTION_PATH);
		if (file.exists() && file.length() > 0) {
			if (size != file.length() && !firstTime) {
				String msg = MessageFormat.format(getString(R.string.previous_run_crashed), OsmandApplication.EXCEPTION_PATH);
				Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
				builder.setMessage(msg).setNeutralButton(getString(R.string.close), null);
				builder.setPositiveButton(R.string.send_report, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "support@nogago.com" }); //$NON-NLS-1$
						intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
						intent.setType("vnd.android.cursor.dir/email"); //$NON-NLS-1$
						intent.putExtra(Intent.EXTRA_SUBJECT, "nogago bug"); //$NON-NLS-1$
						StringBuilder text = new StringBuilder();
						text.append("\nDevice : ").append(Build.DEVICE); //$NON-NLS-1$
						text.append("\nBrand : ").append(Build.BRAND); //$NON-NLS-1$
						text.append("\nModel : ").append(Build.MODEL); //$NON-NLS-1$
						text.append("\nProduct : ").append(Build.PRODUCT); //$NON-NLS-1$
						text.append("\nBuild : ").append(Build.DISPLAY); //$NON-NLS-1$
						text.append("\nVersion : ").append(Build.VERSION.RELEASE); //$NON-NLS-1$
						text.append("\nApp Version : ").append(Version.getAppName(MainMenuActivity.this)); //$NON-NLS-1$
						try {
							PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
							if (info != null) {
								text.append("\nApk Version : ").append(info.versionName).append(" ").append(info.versionCode); //$NON-NLS-1$ //$NON-NLS-2$
							}
						} catch (NameNotFoundException e) {
						}
						intent.putExtra(Intent.EXTRA_TEXT, text.toString());
						startActivity(Intent.createChooser(intent, getString(R.string.send_report)));
					}

				});
				builder.show();
			}
			getPreferences(MODE_WORLD_WRITEABLE).edit().putLong(EXCEPTION_FILE_SIZE, file.length()).commit();
		} else {
			if (size > 0) {
				getPreferences(MODE_WORLD_WRITEABLE).edit().putLong(EXCEPTION_FILE_SIZE, 0).commit();
			}
		}
	}
	*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == APP_EXIT_CODE){
			finish();
		}
	}
	
	public static Animation getAnimation(int left, int top){
		Animation anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, left, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, top, TranslateAnimation.RELATIVE_TO_SELF, 0);
		anim.setDuration(700);
		anim.setInterpolator(new AccelerateInterpolator());
		return anim;
	}
	
	public static void onCreateMainMenu(Window window, final Activity activity){
		View head = (View) window.findViewById(R.id.Headliner);
		head.startAnimation(getAnimation(0, -1));
		
		View leftview = (View) window.findViewById(R.id.MapButton);
		leftview.startAnimation(getAnimation(-1, 0));
		leftview = (View) window.findViewById(R.id.FavoritesButton);
		leftview.startAnimation(getAnimation(-1, 0));
		
		View rightview = (View) window.findViewById(R.id.SettingsButton);
		rightview.startAnimation(getAnimation(1, 0));
		rightview = (View) window.findViewById(R.id.SearchButton);
		rightview.startAnimation(getAnimation(1, 0));
		
		View helpButton = window.findViewById(R.id.HelpButton);
		helpButton.startAnimation(getAnimation(0, 1));
		

		View closeButton = window.findViewById(R.id.CloseButton);
		closeButton.startAnimation(getAnimation(0, 1));
	}
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    trackRecordingServiceConnection = new TrackRecordingServiceConnection(
	        this, bindChangedCallback);

		
		boolean exit = false;
		if(getIntent() != null){
			Intent intent = getIntent();
			if(intent.getExtras() != null && intent.getExtras().containsKey(APP_EXIT_KEY)){
				exit = true;
				finish();
			}
			showStartupDialogs();
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu);
		
		onCreateMainMenu(getWindow(), this);

		Window window = getWindow();
		final Activity activity = this;

		View showMap = window.findViewById(R.id.MapButton); //LO_RecordTrackService
		showMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    trackRecordingServiceConnection.startAndBind();
			    bindChangedCallback.run();
			}
		});
		View settingsButton = window.findViewById(R.id.SettingsButton); //RU_Settings
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent settings = new Intent(activity, SettingsActivity.class);
				activity.startActivity(settings);
			}
		});

		View favouritesButton = window.findViewById(R.id.FavoritesButton); //LU_Statistics
		favouritesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent favorites = new Intent(activity, AggregatedStatsActivity.class);
				favorites.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				activity.startActivity(favorites);
			}
		});
		
		View helpButton = window.findViewById(R.id.HelpButton);
		helpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    final Intent help = new Intent(activity, HelpActivity.class);
			    activity.startActivity(help);
	
			}
		});
		
		View closeButton = window.findViewById(R.id.CloseButton);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.runFinalizersOnExit(true);
				System.exit(0);
			}
		});

		View searchButton = window.findViewById(R.id.SearchButton); //RO_Tracklist
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent search = new Intent(activity, TrackListActivity.class);
				search.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				activity.startActivity(search);
			}
		});
		if(exit){
			return;
		}
	}
	
  
    public void showStartupDialogs() {

       if (EulaUtils.getShowCheckUnits(this)) {
        Fragment fragment = getSupportFragmentManager()
            .findFragmentByTag(CheckUnitsDialogFragment.CHECK_UNITS_DIALOG_TAG);
        if (fragment == null) {
          new CheckUnitsDialogFragment().show(
              getSupportFragmentManager(), CheckUnitsDialogFragment.CHECK_UNITS_DIALOG_TAG);
        }
      } else {
        return;
      }
    }
    
}
