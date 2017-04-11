package com.nogago.android.maps.views;


import com.nogago.android.maps.R;
import com.nogago.android.maps.activities.ShowRouteInfoActivity;
import com.nogago.android.maps.routing.RoutingHelper;
import com.nogago.android.maps.routing.RoutingHelper.IRouteInformationListener;
import com.nogago.android.maps.routing.RoutingHelper.RouteDirectionInfo;

import net.osmand.osm.LatLon;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;


public class RouteInfoLayer extends OsmandMapLayer implements IRouteInformationListener {

	private OsmandMapTileView view;
	private final RoutingHelper routingHelper;
	private Button next;
	private Button prev;
	private Button info;
	private boolean visible = true;
	private int directionInfo = -1;

	private DisplayMetrics dm;
	private final ContextMenuLayer contextMenu;
	
	
	
	public RouteInfoLayer(RoutingHelper routingHelper, LinearLayout layout, ContextMenuLayer contextMenu){
		this.routingHelper = routingHelper;
		this.contextMenu = contextMenu;
		prev = (Button) layout.findViewById(R.id.PreviousButton);
		next = (Button) layout.findViewById(R.id.NextButton);
		info = (Button) layout.findViewById(R.id.InfoButton);
		routingHelper.addListener(this);
		attachListeners();
		updateVisibility();
	}
	
	@Override
	public void initLayer(OsmandMapTileView view) {
		this.view = view;
		dm = new DisplayMetrics();
		WindowManager wmgr = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
		wmgr.getDefaultDisplay().getMetrics(dm);
	}
	
	private void attachListeners() {
		prev.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(routingHelper.getRouteDirections() != null && directionInfo > 0){
					directionInfo--;
					if(routingHelper.getRouteDirections().size() > directionInfo){
						RouteDirectionInfo info = routingHelper.getRouteDirections().get(directionInfo);
						Location l = routingHelper.getLocationFromRouteDirection(info);
						if(info.descriptionRoute != null) {
							contextMenu.setLocation(new LatLon(l.getLatitude(), l.getLongitude()), info.descriptionRoute);
						}
						view.getAnimatedDraggingThread().startMoving(l.getLatitude(), l.getLongitude(), view.getZoom(), true);
					}
				}
				view.refreshMap();
			}
			
		});
		next.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(routingHelper.getRouteDirections() != null && directionInfo < routingHelper.getRouteDirections().size() - 1){
					directionInfo++;
					RouteDirectionInfo info = routingHelper.getRouteDirections().get(directionInfo);
					Location l = routingHelper.getLocationFromRouteDirection(info);
					if(info.descriptionRoute != null){
						contextMenu.setLocation(new LatLon(l.getLatitude(), l.getLongitude()), info.descriptionRoute);
					}
					view.getAnimatedDraggingThread().startMoving(l.getLatitude(), l.getLongitude(), view.getZoom(), true);
				}
				view.refreshMap();
			}
			
		});
		info.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(view.getContext(), ShowRouteInfoActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				view.getContext().startActivity(intent);
			}
		});
	}

	public boolean isVisible(){
		return visible && routingHelper.isRouteCalculated() && !routingHelper.isFollowingMode();
	}
	public boolean couldBeVisible(){
		return routingHelper.isRouteCalculated() && !routingHelper.isFollowingMode();
	}
	private void updateVisibility(){
		int vis = isVisible() ? View.VISIBLE : View.INVISIBLE; 
		prev.setVisibility(vis);
		next.setVisibility(vis);
		info.setVisibility(vis);
	}


	@Override
	public void destroyLayer() {
	}

	@Override
	public boolean drawInScreenPixels() {
		return true;
	}

	@Override
	public boolean onLongPressEvent(PointF point) {
		return false;
	}

	@Override
	public boolean onSingleTap(PointF point) {
		return false;
	}

	@Override
	public void newRouteIsCalculated(boolean updateRoute, boolean suppressTurnPrompt) {
		directionInfo = -1;
		if (!routingHelper.isFollowingMode()) {
			visible = true;
		}
		updateVisibility();
		view.refreshMap();
	}

	public boolean isUserDefinedVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		updateVisibility();
	}
	@Override
	public void routeWasCancelled() {
		directionInfo = -1;
		updateVisibility();
	}

	@Override
	public void onDraw(Canvas canvas, RectF latlonRect, RectF tilesRect, DrawSettings nightMode) {
		
	}
	
	
}
