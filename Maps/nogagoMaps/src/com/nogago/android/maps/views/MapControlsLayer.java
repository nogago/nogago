package com.nogago.android.maps.views;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.OsmAndFormatter;
import com.nogago.android.maps.R;
import com.nogago.android.maps.activities.ApplicationMode;
import com.nogago.android.maps.activities.MapActivity;
import com.nogago.android.maps.activities.MapManagerActivity;
import com.nogago.android.maps.plus.OsmandSettings.CommonPreference;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import net.osmand.osm.LatLon;
import net.osmand.osm.MapUtils;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

public class MapControlsLayer extends OsmandMapLayer {

	private static final int SHOW_SEEKBAR_MSG_ID = 2;
	private static final int SHOW_SEEKBAR_DELAY = 7000;
	private static final int SHOW_SEEKBAR_SECOND_DELAY = 25000;
	
	private static final int SHOW_ZOOM_LEVEL_MSG_ID = 3;
	private static final int SHOW_ZOOM_LEVEL_DELAY = 2000;
	

	private OsmandMapTileView view;
	private DisplayMetrics dm;
	private final MapActivity activity;
	private Handler showUIHandler;
	
	private boolean showZoomLevel = false;
	
	
	private Button zoomInButton;
	private Button zoomOutButton;
	private Button downloadMapButton;
	
	private TextPaint zoomTextPaint;
	private Drawable zoomShadow;
	
	// private Button backToMenuButton;
	private Drawable modeShadow;
	
	private Drawable rulerDrawable; 
	private TextPaint rulerTextPaint;
	private final static double screenRulerPercent = 0.25;
	private SeekBar transparencyBar;
	private CommonPreference<Integer> settingsToTransparency;
	private BaseMapLayer[] transparencyLayers;
	private float scaleCoefficient;
	

	public MapControlsLayer(MapActivity activity){
		this.activity = activity;
	}
	
	
	@Override
	public boolean drawInScreenPixels() {
		return true;
	}

	@Override
	public void initLayer(final OsmandMapTileView view) {
		this.view = view;
		dm = new DisplayMetrics();
		WindowManager wmgr = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
		wmgr.getDefaultDisplay().getMetrics(dm);
		scaleCoefficient = dm.density;
		if (Math.min(dm.widthPixels / (dm.density * 160), dm.heightPixels / (dm.density * 160)) > 2.5f) {
			// large screen
			scaleCoefficient *= 1.5f;
		}
		FrameLayout parent = (FrameLayout) view.getParent();
		showUIHandler = new Handler();
		
		initZoomButtons(view, parent);

		// initBackToMenuButton(view, parent);
		
		initRuler(view, parent);
		
		initTransparencyBar(view, parent);
		
	}

	@Override
	public void destroyLayer() {
	}

	@Override
	public void onDraw(Canvas canvas, RectF latlonRect, RectF tilesRect, DrawSettings nightMode) {
		BaseMapLayer mainLayer = view.getMainLayer();
		boolean zoomInEnabled = mainLayer != null && view.getZoom() < mainLayer.getMaximumShownMapZoom();
		boolean zoomOutEnabled = mainLayer != null && view.getZoom() > mainLayer.getMinimumShownMapZoom();
		if(zoomInButton.isEnabled() != zoomInEnabled){
			zoomInButton.setEnabled(zoomInEnabled);
		}
		if(zoomOutButton.isEnabled() != zoomOutEnabled){
			zoomOutButton.setEnabled(zoomOutEnabled);
		}
		
//		drawApplicationMode(canvas); //app-mode-Switcher in MapView
		
		if(view.isZooming()){
			showZoomLevel = true;
			showUIHandler.removeMessages(SHOW_ZOOM_LEVEL_MSG_ID);
		} else {
			if(showZoomLevel){
				hideZoomLevelInTime();
			}
		}
		if (showZoomLevel || !view.getSettings().SHOW_RULER.get()) {
			drawZoomLevel(canvas);
		} else {
			drawRuler(canvas);
		}
	}


	private ApplicationMode cacheApplicationMode = null;
	private Drawable cacheAppModeIcon = null;
	private void drawApplicationMode(Canvas canvas) {
		ApplicationMode  appMode = view.getSettings().getApplicationMode();
		if(appMode != cacheApplicationMode){
			/*
			modeShadow.setBounds(backToMenuButton.getLeft() + (int) (2 * scaleCoefficient), backToMenuButton.getTop() - (int) (20 * scaleCoefficient),
					backToMenuButton.getRight() - (int) (4 * scaleCoefficient), backToMenuButton.getBottom());
			
			if(appMode == ApplicationMode.CAR){
				cacheAppModeIcon = view.getResources().getDrawable(R.drawable.car_small);
			} else 
			*/
			if(appMode == ApplicationMode.BICYCLE){
				cacheAppModeIcon = view.getResources().getDrawable(R.drawable.bicycle_small);
			} else {
				cacheAppModeIcon = view.getResources().getDrawable(R.drawable.pedestrian_small);
			}
			/*else {
				cacheAppModeIcon = view.getResources().getDrawable(R.drawable.default_mode_small);
			}
			*/
			int l = modeShadow.getBounds().left + (modeShadow.getBounds().width() - cacheAppModeIcon.getMinimumWidth()) / 2;
			int t = (int) (modeShadow.getBounds().top + 5 * scaleCoefficient);
			cacheAppModeIcon.setBounds(l, t, l + cacheAppModeIcon.getMinimumWidth(), t + cacheAppModeIcon.getMinimumHeight());	
		}
		modeShadow.draw(canvas);
		if(cacheAppModeIcon != null){
			cacheAppModeIcon.draw(canvas);
		}
		
	}
	/*
	private void onApplicationModePress() {
		final QuickAction mQuickAction = new QuickAction(backToMenuButton);
		int[] icons = new int[] {R.drawable.pedestrian_small, R.drawable.bicycle_small};
		int[] values = new int[] {R.string.app_mode_pedestrian, R.string.app_mode_bicycle };
		final ApplicationMode[] modes = new ApplicationMode[] {ApplicationMode.PEDESTRIAN,  ApplicationMode.BICYCLE};
		for (int i = 0; i < modes.length; i++) {
			final ActionItem action = new ActionItem();
			action.setTitle(view.getResources().getString(values[i]));
			action.setIcon(view.getResources().getDrawable(icons[i]));
			final int j = i;
			action.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					view.getSettings().APPLICATION_MODE.set(modes[j]);
					activity.updateApplicationModeSettings();
					view.refreshMap(true);
					mQuickAction.dismiss();
				}
			});
			mQuickAction.addActionItem(action);
		}
		mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
		mQuickAction.show();
	}
	*/
	
	private void drawZoomLevel(Canvas canvas) {
		ShadowText zoomText = ShadowText.create(view.getZoom() + "");
		float length = zoomTextPaint.measureText(zoomText.getText());
		if (zoomShadow.getBounds().width() == 0) {
			zoomShadow.setBounds(zoomInButton.getLeft() - 2, zoomInButton.getTop() - (int) (18 * scaleCoefficient), zoomInButton.getRight(),
					zoomInButton.getBottom());
		}
		zoomShadow.draw(canvas);
				
		zoomText.draw(canvas, zoomInButton.getLeft() + (zoomInButton.getWidth() - length - 2) / 2,
				zoomInButton.getTop() + 4 * scaleCoefficient, zoomTextPaint);
	}
	
	private void hideZoomLevelInTime(){
		if (!showUIHandler.hasMessages(SHOW_ZOOM_LEVEL_MSG_ID)) {
			Message msg = Message.obtain(showUIHandler, new Runnable() {
				@Override
				public void run() {
					showZoomLevel = false;
					view.refreshMap();
				}

			});
			msg.what = SHOW_ZOOM_LEVEL_MSG_ID;
			showUIHandler.sendMessageDelayed(msg, SHOW_ZOOM_LEVEL_DELAY);
		}
	}

/*
	@Override
	public boolean onSingleTap(PointF point) {
		if (modeShadow.getBounds().contains((int) point.x, (int) point.y)) {
			onApplicationModePress();
			return true;
		}
		return false;
	}
	
	private void initBackToMenuButton(final OsmandMapTileView view, FrameLayout parent) {
		android.widget.FrameLayout.LayoutParams params;
		backToMenuButton = new Button(view.getContext());
		backToMenuButton.setBackgroundResource(R.drawable.map_btn_menu);
		params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM | Gravity.LEFT);
		parent.addView(backToMenuButton, params);
		backToMenuButton.setEnabled(true);
		
		modeShadow = view.getResources().getDrawable(R.drawable.zoom_background);
		
		
		backToMenuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.backToMainMenu();
			}
		});
	}
	
*/
	private void initRuler(OsmandMapTileView view, FrameLayout parent) {
		rulerTextPaint = new TextPaint();
		rulerTextPaint.setTextSize(20 * scaleCoefficient);
		rulerTextPaint.setAntiAlias(true);
		
		
		rulerDrawable = view.getResources().getDrawable(R.drawable.ruler);
	}
	
	private void initZoomButtons(final OsmandMapTileView view, FrameLayout parent) {
		int minimumWidth = view.getResources().getDrawable(R.drawable.map_zoom_in).getMinimumWidth();
		
		Context ctx = view.getContext();
		ImageView bottomShadow = new ImageView(ctx);
		bottomShadow.setBackgroundResource(R.drawable.bottom_shadow);
		android.widget.FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM);
		params.setMargins(0, 0, 0, 0);
		parent.addView(bottomShadow, params);
		
		zoomTextPaint = new TextPaint();
		zoomTextPaint.setTextSize(18 * scaleCoefficient);
		zoomTextPaint.setAntiAlias(true);
		zoomTextPaint.setFakeBoldText(true);
		
		zoomShadow = view.getResources().getDrawable(R.drawable.zoom_background);
		
		zoomInButton = new Button(ctx);
		zoomInButton.setBackgroundResource(R.drawable.map_zoom_in);
		params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM | Gravity.RIGHT);
		params.setMargins(0, 0, 0, 0);
		zoomInButton.setContentDescription(ctx.getString(R.string.zoomIn));
		parent.addView(zoomInButton, params);
		
		
		zoomOutButton = new Button(ctx);
		zoomOutButton.setBackgroundResource(R.drawable.map_zoom_out);
		params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM | Gravity.RIGHT);
		
		params.setMargins(0, 0, minimumWidth , 0);
		zoomOutButton.setContentDescription(ctx.getString(R.string.zoomOut));
		parent.addView(zoomOutButton, params);
		
		downloadMapButton = new Button(ctx);
		downloadMapButton.setBackgroundResource(R.drawable.map_download);
		downloadMapButton.setVisibility(View.INVISIBLE);
		params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		
		params.setMargins(0, 0, 0 , 0);
		downloadMapButton.setContentDescription(ctx.getString(R.string.zoomOut));
		parent.addView(downloadMapButton, params);
		
		zoomInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(view.isZooming()){
					activity.changeZoom(view.getZoom() + 2);
				} else {
					activity.changeZoom(view.getZoom() + 1);
				}
				
			}
		});
		
		zoomOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.changeZoom(view.getZoom() - 1);
				
			}
		});
		
		downloadMapButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startMapManagerIntent();
				
			}
		});
	}
	
	private void startMapManagerIntent() {
		Intent intent = new Intent(activity, MapManagerActivity.class);
		LatLon latlon = view.getLatLonFromScreenPoint(view.getCenterPointX(), view.getCenterPointY());
		intent.putExtra(Constants.STRING_LONGITUDE, latlon.getLongitude());
		intent.putExtra(Constants.STRING_LATITUDE, latlon.getLatitude());
		activity.startActivity(intent);
		/*
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(Constants.NOGAGO_MAP_MANAGER_COMPONENT,
				Constants.NOGAGO_MAP_DOWNLOADER_ACTIVITY));
		ResolveInfo resolved = activity.getPackageManager().resolveActivity(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (resolved != null) {
			LatLon latlon = view.getLatLonFromScreenPoint(view.getCenterPointX(), view.getCenterPointY());
			intent.putExtra(Constants.STRING_LONGITUDE, latlon.getLongitude());
			intent.putExtra(Constants.STRING_LATITUDE, latlon.getLatitude());
			activity.startActivity(intent);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(activity.getString(R.string.map_manager_app_not_found));
			builder.setPositiveButton(
					activity.getString(R.string.default_buttons_yes),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							Intent intent = new Intent(Intent.ACTION_VIEW,
									Uri.parse("market://search?q=pname:"
											+ Constants.NOGAGO_MAP_MANAGER_COMPONENT));
							try {
								activity.startActivity(intent);
							} catch (ActivityNotFoundException e) {
							}
						}
					});
			builder.setNegativeButton(
					activity.getString(R.string.default_buttons_no), null);
			builder.show();
		}
		*/
	}
	
	public void showDownloadMapButton(){
		downloadMapButton.setVisibility(View.VISIBLE);
	}
	
	public void hideDownloadMapButton(){
		downloadMapButton.setVisibility(View.INVISIBLE);
	}


	/////////////////  Transparency bar /////////////////////////
	private void initTransparencyBar(final OsmandMapTileView view, FrameLayout parent) {
		int minimumHeight = view.getResources().getDrawable(R.drawable.map_zoom_in).getMinimumHeight();
		android.widget.FrameLayout.LayoutParams params;
		transparencyBar = new SeekBar(view.getContext());
		transparencyBar.setVisibility(View.GONE);
		transparencyBar.setMax(255);
		params = new FrameLayout.LayoutParams((int) (scaleCoefficient * 100), LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM | Gravity.CENTER);
		params.setMargins(0, 0, 0, minimumHeight + 3);
		parent.addView(transparencyBar, params);
		transparencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(settingsToTransparency != null && transparencyLayers != null){
					settingsToTransparency.set(progress);
					for(BaseMapLayer base : transparencyLayers){
						base.setAlpha(progress);
					}
					MapControlsLayer.this.view.refreshMap();
					showAndHideTransparencyBar(settingsToTransparency, transparencyLayers, SHOW_SEEKBAR_SECOND_DELAY);
				}
			}
		});
	}
	
	public void showAndHideTransparencyBar(CommonPreference<Integer> transparenPreference,
			BaseMapLayer[] layerToChange) {
		showAndHideTransparencyBar(transparenPreference, layerToChange, SHOW_SEEKBAR_DELAY);
	}
	private void showAndHideTransparencyBar(CommonPreference<Integer> transparenPreference,
			BaseMapLayer[] layerToChange, int delay) {
		transparencyBar.setVisibility(View.VISIBLE);
		transparencyBar.setProgress(transparenPreference.get());
		this.transparencyLayers = layerToChange;
		this.settingsToTransparency = transparenPreference;
		Message msg = Message.obtain(showUIHandler, new Runnable() {
			@Override
			public void run() {
				transparencyBar.setVisibility(View.GONE);
			}

		});
		msg.what = SHOW_SEEKBAR_MSG_ID;
		showUIHandler.removeMessages(SHOW_SEEKBAR_MSG_ID);
		showUIHandler.sendMessageDelayed(msg, delay);
	}
	
	
	/////////////////////// Ruler ///////////////////
	// cache values for ruler
	ShadowText cacheRulerText = null;
	int cacheRulerZoom = 0;
	double cacheRulerTileX = 0;
	double cacheRulerTileY = 0;
	float cacheRulerTextLen = 0;	
	
	private void drawRuler(Canvas canvas) {
		// update cache
		if (view.isZooming()) {
			cacheRulerText = null;
		} else if(view.getZoom() != cacheRulerZoom || 
				Math.abs(view.getXTile() - cacheRulerTileX) +  Math.abs(view.getYTile() - cacheRulerTileY) > 1){
			cacheRulerZoom = view.getZoom();
			cacheRulerTileX = view.getXTile();
			cacheRulerTileY = view.getYTile();
			double latitude = view.getLatitude();
			double tileNumberLeft = cacheRulerTileX - ((double) view.getWidth()) / (2d * view.getTileSize());
			double tileNumberRight = cacheRulerTileX + ((double) view.getWidth()) / (2d * view.getTileSize());
			double dist = MapUtils.getDistance(latitude, MapUtils.getLongitudeFromTile(view.getZoom(), tileNumberLeft), latitude,
					MapUtils.getLongitudeFromTile(view.getZoom(), tileNumberRight));
			double pixDensity = view.getWidth() / dist; 
			
			double roundedDist = OsmAndFormatter.calculateRoundedDist(dist * screenRulerPercent, view.getContext());
			
			int cacheRulerDistPix = (int) (pixDensity * roundedDist);
			cacheRulerText = ShadowText.create(OsmAndFormatter.getFormattedDistance((float) roundedDist, view.getContext()));
			cacheRulerTextLen = zoomTextPaint.measureText(cacheRulerText.getText());
			
			Rect bounds = rulerDrawable.getBounds();
			bounds.right = (int) (view.getWidth() - 7 * scaleCoefficient);
			bounds.bottom = (int) (view.getHeight() - view.getResources().getDrawable(R.drawable.map_zoom_in).getMinimumHeight());
			bounds.top = bounds.bottom - rulerDrawable.getMinimumHeight();
			bounds.left = bounds.right - cacheRulerDistPix;
			rulerDrawable.setBounds(bounds);
		} 
		if (cacheRulerText != null) {
			rulerDrawable.draw(canvas);
			Rect bounds = rulerDrawable.getBounds();
			cacheRulerText.draw(canvas, bounds.left + (bounds.width() - cacheRulerTextLen) / 2, bounds.bottom - 8 * scaleCoefficient,
					rulerTextPaint);
		}
	}


	

}
