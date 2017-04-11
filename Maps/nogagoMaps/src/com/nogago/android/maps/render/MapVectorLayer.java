package com.nogago.android.maps.render;

import com.nogago.android.maps.NogagoUtils;
import com.nogago.android.maps.plus.ResourceManager;
import com.nogago.android.maps.plus.RotatedTileBox;
import com.nogago.android.maps.views.BaseMapLayer;
import com.nogago.android.maps.views.MapControlsLayer;
import com.nogago.android.maps.views.MapTileLayer;
import com.nogago.android.maps.views.OsmandMapTileView;
import com.nogago.android.maps.views.RouteInfoLayer;

import net.osmand.osm.MapUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class MapVectorLayer extends BaseMapLayer {

	private OsmandMapTileView view;
	private Rect pixRect = new Rect();
	private RotatedTileBox rotatedTileBox = new RotatedTileBox(0, 0, 0, 0, 0, 0);
	private ResourceManager resourceManager;
	private Paint paintImg;
	
	private RectF destImage = new RectF();
	private final MapTileLayer tileLayer;
	private boolean visible = false;
	
	public MapVectorLayer(MapTileLayer tileLayer){
		this.tileLayer = tileLayer;
	}
	

	@Override
	public void destroyLayer() {
	}

	@Override
	public boolean drawInScreenPixels() {
		return false;
	}

	@Override
	public void initLayer(OsmandMapTileView view) {
		this.view = view;
		resourceManager = view.getApplication().getResourceManager();
		paintImg = new Paint();
		paintImg.setFilterBitmap(true);
		paintImg.setAlpha(getAlpha());
	}
	
	private void updateRotatedTileBox(){
		float ts = view.getTileSize();
		float xL = view.calcDiffTileX(pixRect.left - view.getCenterPointX(), pixRect.top - view.getCenterPointY()) + view.getXTile();
		float yT = view.calcDiffTileY(pixRect.left - view.getCenterPointX(), pixRect.top - view.getCenterPointY()) + view.getYTile();
		rotatedTileBox.set(xL, yT, ((float) pixRect.width()) / ts, ((float) pixRect.height()) / ts, view.getRotate(), view.getZoom());
	}
	
	public boolean isVectorDataVisible() {
		return visible &&  view.getZoom() >= view.getSettings().LEVEL_TO_SWITCH_VECTOR_RASTER.get();
	}
	
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		if(!visible){
			resourceManager.getRenderer().clearCache();
		}
	}
	
	@Override
	public int getMaximumShownMapZoom() {
		return 23;
	}
	
	@Override
	public int getMinimumShownMapZoom() {
		return 1;
	}
	

	@Override
	public void onDraw(Canvas canvas, RectF latLonBounds, RectF tilesRect, DrawSettings drawSettings) {
		if(!visible){
			return;
		}
		MapControlsLayer controlsLayer = view.getMapControlsLayer();
		if(!isVectorDataVisible() && tileLayer != null){
			tileLayer.drawTileMap(canvas, tilesRect);
			resourceManager.getRenderer().interruptLoadingMap();
			if(controlsLayer != null && NogagoUtils.isOnline(view.getContext())){
				
				RouteInfoLayer routeInfoLayer = view.getRouteInfoLayer(); 
				if(routeInfoLayer == null || !routeInfoLayer.isVisible()){
					controlsLayer.showDownloadMapButton();
				}else{
					controlsLayer.hideDownloadMapButton();
				}
				
				view.setCurrentMapOffline(false);
			}
		} else {
			if(resourceManager.getRenderer().containsLatLonMapData(view.getLatitude(), view.getLongitude(), view.getZoom())){
				if (!view.isZooming()) {
					pixRect.set(0, 0, view.getWidth(), view.getHeight());
					updateRotatedTileBox();
					//TODO passing of nithMode and appMode could be probably something more general? These are
					//renderer properties, so, we should check if renderer properties are changed somehow...
					if (resourceManager.updateRenderedMapNeeded(rotatedTileBox,drawSettings)) {
						// pixRect.set(-view.getWidth(), -view.getHeight() / 2, 2 * view.getWidth(), 3 * view.getHeight() / 2);
						pixRect.set(-view.getWidth() / 3, -view.getHeight() / 4, 4 * view.getWidth() / 3, 5 * view.getHeight() / 4);
						updateRotatedTileBox();
						resourceManager.updateRendererMap(rotatedTileBox);
						// does it slow down Map refreshing ?
						// Arguments : 1. Map request to read data slows whole process // 2. It works in operating memory
						//if (warningToSwitchMapShown < 3) {
							if (!resourceManager.getRenderer().containsLatLonMapData(view.getLatitude(), view.getLongitude(), view.getZoom())) {
								//AccessibleToast.makeText(view.getContext(), R.string.switch_to_raster_map_to_see, Toast.LENGTH_LONG).show();
								//warningToSwitchMapShown++;
							}
						//}
					}
	
				}
				MapRenderRepositories renderer = resourceManager.getRenderer();
				drawRenderedMap(canvas, renderer.getBitmap(), renderer.getBitmapLocation());
				drawRenderedMap(canvas, renderer.getPrevBitmap(), renderer.getPrevBmpLocation());
				if(controlsLayer != null){
					controlsLayer.hideDownloadMapButton();
					view.setCurrentMapOffline(true);
				}
			}else{
				tileLayer.drawTileMap(canvas, tilesRect);
				resourceManager.getRenderer().interruptLoadingMap();
				if(controlsLayer != null && NogagoUtils.isOnline(view.getContext())){
					
					RouteInfoLayer routeInfoLayer = view.getRouteInfoLayer(); 
					if(routeInfoLayer == null || !routeInfoLayer.isVisible()){
						controlsLayer.showDownloadMapButton();
					}else{
						controlsLayer.hideDownloadMapButton();
					}
					
					view.setCurrentMapOffline(false);
				}
			}
		}
	}


	private void drawRenderedMap(Canvas canvas, Bitmap bmp, RotatedTileBox bmpLoc) {
		if (bmp != null && bmpLoc != null) {
			float rot = bmpLoc.getRotate();
			float mult = (float) MapUtils.getPowZoom(view.getZoom() - bmpLoc.getZoom());
			
			float tx = view.getXTile();
			float ty = view.getYTile();
			float dleftX1 = (bmpLoc.getLeftTileX() * mult - tx) ;
			float dtopY1 =  (bmpLoc.getTopTileY() * mult - ty);
			
			
			float cos = bmpLoc.getRotateCos();
			float sin = bmpLoc.getRotateSin();
			float x1 = MapUtils.calcDiffPixelX(sin, cos, dleftX1, dtopY1, view.getTileSize()) + view.getCenterPointX();
			float y1 = MapUtils.calcDiffPixelY(sin, cos, dleftX1, dtopY1, view.getTileSize()) + view.getCenterPointY();
			
			canvas.rotate(-rot, view.getCenterPointX(), view.getCenterPointY());
			destImage.set(x1, y1, x1 + bmpLoc.getTileWidth() * mult * view.getTileSize(), y1 + bmpLoc.getTileHeight() * mult
					* view.getTileSize());
			if(!bmp.isRecycled()){
				canvas.drawBitmap(bmp, null, destImage, paintImg);
			}
			canvas.rotate(rot, view.getCenterPointX(), view.getCenterPointY());
		}
	}

	
	@Override
	public void setAlpha(int alpha) {
		super.setAlpha(alpha);
		if (paintImg != null) {
			paintImg.setAlpha(alpha);
		}
	}	
	
	@Override
	public boolean onLongPressEvent(PointF point) {
		return false;
	}

	@Override
	public boolean onSingleTap(PointF point) {
		return false;
	}
}
