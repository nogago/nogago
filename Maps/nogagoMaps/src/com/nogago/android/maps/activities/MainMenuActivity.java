package com.nogago.android.maps.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.R;
import com.nogago.android.maps.Version;
import com.nogago.android.maps.activities.search.SearchActivity;
import com.nogago.android.maps.plus.OsmandApplication;
import com.nogago.android.maps.render.MapRenderRepositories;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

	public static final int APP_EXIT_CODE = 4;
	public static final String APP_EXIT_KEY = "APP_EXIT_KEY";

	private ProgressDialog startProgressDialog;

	private void checkPriorExceptions(boolean firstTime) {
		final File file = OsmandApplication.getSettings().extendOsmandPath(
				OsmandApplication.EXCEPTION_PATH);
		if (file != null && file.exists() && file.length() > 0) {
			String msg = getString(R.string.previous_run_crashed);
			Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
			// User says no
			builder.setMessage(msg).setNeutralButton(getString(R.string.close),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Delete Exceptions File when user presses Ignore
							if (!file.delete())
								Toast.makeText(getApplicationContext(),
										"Exceptions file not deleted",
										Toast.LENGTH_LONG).show();
						}
					});
			// User says yes
			builder.setPositiveButton(R.string.send_report,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Intent.ACTION_SEND);
							intent.putExtra(Intent.EXTRA_EMAIL,
									new String[] { Constants.BUGS_MAIL }); //$NON-NLS-1$
							intent.setType("vnd.android.cursor.dir/email"); //$NON-NLS-1$
							intent.putExtra(Intent.EXTRA_SUBJECT,
									"nogago Maps bug"); //$NON-NLS-1$
							StringBuilder text = new StringBuilder();
							text.append("\nDevice : ").append(Build.DEVICE); //$NON-NLS-1$
							text.append("\nBrand : ").append(Build.BRAND); //$NON-NLS-1$
							text.append("\nModel : ").append(Build.MODEL); //$NON-NLS-1$
							text.append("\nProduct : ").append("Maps"); //$NON-NLS-1$
							text.append("\nBuild : ").append(Build.DISPLAY); //$NON-NLS-1$
							text.append("\nVersion : ").append(Build.VERSION.RELEASE); //$NON-NLS-1$
							text.append("\nApp Starts : ").append(com.nogago.android.maps.utils.EulaUtils.getAppStart(MainMenuActivity.this)).append("\n"); //$NON-NLS-1$

							try {
								PackageInfo info = getPackageManager()
										.getPackageInfo(getPackageName(), 0);
								if (info != null) {
									text.append("\nApk Version : ").append(info.versionName).append(" ").append(info.versionCode).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
								}
							} catch (NameNotFoundException e) {
							}

							try {
								FileReader fr = new FileReader(file);
								BufferedReader br = new BufferedReader(fr);
								String line = null;
								do {
									line = br.readLine();
									if(line != null) text.append(line).append("\n");
								} while (line != null);
								br.close();
								fr.close();
							} catch (IOException e) {
								Toast.makeText(getApplicationContext(),
										"Error reading exceptions file!",
										Toast.LENGTH_LONG).show();
							}
							intent.putExtra(Intent.EXTRA_TEXT, text.toString());
							startActivity(Intent.createChooser(intent,
									getString(R.string.send_report)));

							if (!file.delete())
								Toast.makeText(getApplicationContext(),
										"Exceptions file not deleted",
										Toast.LENGTH_LONG).show();
						}

					});
			builder.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == APP_EXIT_CODE) {
			finish();
		}
	}

	public static Animation getAnimation(int left, int top) {
		Animation anim = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, left,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, top,
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		anim.setDuration(700);
		anim.setInterpolator(new AccelerateInterpolator());
		return anim;
	}

	/*
	 * public static void onCreateMainMenu(Window window, final Activity
	 * activity) {
	 * 
	 * View head = (View) window.findViewById(R.id.Headliner); //
	 * head.startAnimation(getAnimation(0, -1));
	 * 
	 * View leftview = (View) window.findViewById(R.id.MapButton); //
	 * leftview.startAnimation(getAnimation(0, -1)); leftview = (View)
	 * window.findViewById(R.id.FavoritesButton); //
	 * leftview.startAnimation(getAnimation(-1, 0)); leftview = (View)
	 * window.findViewById(R.id.MapManagerButton); //
	 * leftview.startAnimation(getAnimation(0, 1));
	 * 
	 * View rightview = (View) window.findViewById(R.id.SearchButton); //
	 * rightview.startAnimation(getAnimation(0, -1)); rightview = (View)
	 * window.findViewById(R.id.SettingsButton); //
	 * rightview.startAnimation(getAnimation(1, 0)); rightview = (View)
	 * window.findViewById(R.id.ToTracksButton); //
	 * rightview.startAnimation(getAnimation(0, 1));
	 * 
	 * // View footer = (View) window.findViewById(R.id.MapManager); //
	 * footer.startAnimation(getAnimation(0, 1));
	 * 
	 * /* SharedPreferences prefs =
	 * activity.getApplicationContext().getSharedPreferences
	 * (OsmandSettings.SHARED_PREFERENCES_NAME, MODE_WORLD_READABLE);
	 * 
	 * // only one commit should be with contribution version flag //
	 * prefs.edit().putBoolean(CONTRIBUTION_VERSION_FLAG, true).commit(); if
	 * (prefs.contains(CONTRIBUTION_VERSION_FLAG)) { SpannableString content =
	 * new SpannableString(textVersion); content.setSpan(new ClickableSpan() {
	 * 
	 * @Override public void onClick(View widget) { final Intent mapIntent = new
	 * Intent(activity, ContributionVersionActivity.class);
	 * activity.startActivityForResult(mapIntent, 0); } }, 0, content.length(),
	 * 0); textVersionView.setText(content);
	 * textVersionView.setMovementMethod(LinkMovementMethod.getInstance()); }
	 * View helpButton = window.findViewById(R.id.HelpButton);
	 * helpButton.startAnimation(getAnimation(0, 1));
	 * 
	 * View closeButton = window.findViewById(R.id.CloseButton);
	 * closeButton.startAnimation(getAnimation(0, 1));
	 * 
	 * }
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean exit = false;
		if (getIntent() != null) {
			Intent intent = getIntent();
			if (intent.getExtras() != null
					&& intent.getExtras().containsKey(APP_EXIT_KEY)) {
				exit = true;
				finish();
			}
		}
		final Activity activity = this;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu);

		Window window = getWindow();
		// final Activity activity = this;
		View showMap = window.findViewById(R.id.MapButton);
		showMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent mapIndent = new Intent(activity, MapActivity.class);
				activity.startActivityForResult(mapIndent, 0);
			}
		});
		View settingsButton = window.findViewById(R.id.SettingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent settings = new Intent(activity,
						SettingsActivity.class);
				activity.startActivity(settings);
			}
		});

		View favouritesButton = window.findViewById(R.id.FavoritesButton);
		favouritesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent favorites = new Intent(activity,
						FavouritesActivity.class);
				favorites.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				activity.startActivity(favorites);
			}
		});
		View mapManagerButton = window.findViewById(R.id.MapManagerButton);
		mapManagerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent mapManager = new Intent(activity,
						MapManagerActivity.class);
				mapManager.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				activity.startActivity(mapManager);
			}
		});
		View toTracksButton = window.findViewById(R.id.ToTracksButton);
		toTracksButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					final Intent toTracks = new Intent();
					toTracks.setComponent(new ComponentName(
							Constants.IS_BLACKBERRY ? Constants.BB_TRACKS_PACKAGE
									: Constants.TRACKS_PACKAGE,
							"com.google.android.apps.mytracks.TrackListActivity"));
					toTracks.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					activity.startActivity(toTracks);
				} catch (ActivityNotFoundException e) {
					// Alert if nogago Tracks isnt installed
					AlertDialog.Builder notInstalled = new AlertDialog.Builder(
							MainMenuActivity.this);
					notInstalled
							.setMessage(R.string.tracks_not_installed)
							.setCancelable(false)
							.setPositiveButton(R.string.button_yes,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											Uri uri = Uri
													.parse(Constants.IS_BLACKBERRY ? Constants.BB_TRACKS_DOWNLOAD_URL
															: Constants.PLAY_TRACKS_DOWNLOAD_URL);
											Intent showUri = new Intent(
													Intent.ACTION_VIEW, uri);
											activity.startActivity(showUri);
										}
									})
							.setNegativeButton(R.string.button_no,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					notInstalled.create().show();
				}
			}
		});

		View helpButton = window.findViewById(R.id.HelpButton);
		helpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TipsAndTricksActivity tactivity = new TipsAndTricksActivity(
						activity);
				Dialog dlg = tactivity.getDialogToShowTips(false, true);
				dlg.show();
			}
		});

		View closeButton = window.findViewById(R.id.CloseButton);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getMyApplication().closeApplication();
				// moveTaskToBack(true);
				activity.finish();
				// this is different from MapActivity close...
				if (getMyApplication().getNavigationService() == null) {
					// http://stackoverflow.com/questions/2092951/how-to-close-android-application
					System.runFinalizersOnExit(true);
					System.exit(0);
				}
			}
		});

		View searchButton = window.findViewById(R.id.SearchButton);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent search = new Intent(activity, SearchActivity.class);
				search.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				activity.startActivity(search);
			}
		});
		if (exit) {
			return;
		}
		//
		// View mapManagerButton = window.findViewById(R.id.MapManager);
		// mapManagerButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// final Intent mapManager = new Intent(activity,
		// MapManagerActivity.class);
		// mapManager.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		// activity.startActivity(mapManager);
		// }
		// });

		startProgressDialog = new ProgressDialog(this);
		getMyApplication().checkApplicationIsBeingInitialized(this,
				startProgressDialog);
		SharedPreferences pref = getPreferences(MODE_WORLD_WRITEABLE);
		boolean firstTime = false;
		if (!pref.contains(FIRST_TIME_APP_RUN)) {
			firstTime = true;
			pref.edit().putBoolean(FIRST_TIME_APP_RUN, true).commit();
			pref.edit()
					.putString(VERSION_INSTALLED,
							Version.getFullVersion(activity)).commit();

			applicationInstalledFirstTime();
		} else {
			int i = pref.getInt(TIPS_SHOW, 0);
			if (i < 7) {
				pref.edit().putInt(TIPS_SHOW, ++i).commit();
			}
			boolean appVersionChanged = false;
			if (!Version.getFullVersion(activity).equals(
					pref.getString(VERSION_INSTALLED, ""))) {
				pref.edit()
						.putString(VERSION_INSTALLED,
								Version.getFullVersion(activity)).commit();
				appVersionChanged = true;
			}

			if (i == 1 || i == 5 || appVersionChanged) {
				TipsAndTricksActivity tipsActivity = new TipsAndTricksActivity(
						this);
				Dialog dlg = tipsActivity.getDialogToShowTips(
						!appVersionChanged, false);
				dlg.show();
			}

			/*
			 * else { if (startProgressDialog.isShowing()) {
			 * startProgressDialog.setOnDismissListener(new
			 * DialogInterface.OnDismissListener() {
			 * 
			 * @Override public void onDismiss(DialogInterface dialog) {
			 * checkBaseMapDownloaded(); } }); } else {
			 * checkBaseMapDownloaded(); } }
			 */
		}
		checkPriorExceptions(firstTime);
		if (com.nogago.android.maps.utils.EulaUtils.getShowReview(this)
				&& com.nogago.android.maps.utils.EulaUtils.getAppStart(this) > 7) { 
			// Ask For Review at 7th start, continue bugging the user
			Fragment fragment = getSupportFragmentManager()
					.findFragmentByTag(
							com.nogago.android.maps.views.ReviewDialogFragment.REVIEW_DIALOG_TAG);
			if (fragment == null) {
				com.nogago.android.maps.views.ReviewDialogFragment
						.newInstance(false)
						.show(getSupportFragmentManager(),
								com.nogago.android.maps.views.ReviewDialogFragment.REVIEW_DIALOG_TAG);
			}
		}
	}

	protected void notavailableToast() {
		Toast.makeText(this, getString(R.string.not_available_on_BB),
				Toast.LENGTH_LONG).show();
	}

	private void applicationInstalledFirstTime() {
		boolean netOsmandWasInstalled = false;
		try {
			ApplicationInfo applicationInfo = getPackageManager()
					.getApplicationInfo(Constants.APP_PACKAGE_NAME,
							PackageManager.GET_META_DATA);
			netOsmandWasInstalled = applicationInfo != null;
		} catch (NameNotFoundException e) {
			netOsmandWasInstalled = false;
		}
		/*
		 * if(netOsmandWasInstalled){
		 * 
		 * Do nothing Builder builder = new AlertDialog.Builder(this);
		 * builder.setMessage(R.string.osmand_net_previously_installed);
		 * builder.setPositiveButton(R.string.default_buttons_ok, null);
		 * builder.show();
		 * 
		 * 
		 * } else
		 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.first_time_msg);
		builder.setNegativeButton(R.string.first_time_continue, null);
		builder.setPositiveButton(R.string.first_time_download,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(MainMenuActivity.this,
								SettingsActivity.class));
					}

				});
		builder.show();
		// }
	}

	protected void checkBaseMapDownloaded() {
		MapRenderRepositories maps = getMyApplication().getResourceManager()
				.getRenderer();
		SharedPreferences pref = getPreferences(MODE_WORLD_WRITEABLE);
		boolean check = pref.getBoolean(VECTOR_INDEXES_CHECK, true);
		// do not show each time
		if (check && new Random().nextInt() % 5 == 1) {
			Builder builder = new AlertDialog.Builder(this);
			if (maps.isEmpty()) {
				builder.setMessage(R.string.vector_data_missing);
			} else if (!maps.basemapExists()) {
				builder.setMessage(R.string.basemap_missing);
			} else {
				return;
			}
			builder.setPositiveButton(R.string.download_files,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(MainMenuActivity.this,
									DownloadIndexActivity.class));
						}

					});
			builder.setNeutralButton(R.string.vector_map_not_needed,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							getPreferences(MODE_WORLD_WRITEABLE).edit()
									.putBoolean(VECTOR_INDEXES_CHECK, false)
									.commit();
						}
					});
			builder.setNegativeButton(R.string.first_time_continue, null);
			builder.show();
		}
	}

	private OsmandApplication getMyApplication() {
		return (OsmandApplication) getApplication();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == OsmandApplication.PROGRESS_DIALOG) {
			return startProgressDialog;
		}
		return super.onCreateDialog(id);
	}

	/*
	 * Few Devices have a search key
	 * 
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	 * 
	 * if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
	 * final Intent search = new Intent(MainMenuActivity.this,
	 * SearchActivity.class);
	 * search.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	 * startActivity(search); return true; } return super.onKeyDown(keyCode,
	 * event); }
	 */

}
