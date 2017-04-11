package com.nogago.android.maps.views;

import java.util.ArrayList;
import java.util.List;

import com.nogago.android.maps.R;
import com.nogago.android.maps.activities.MapActivity;

import net.osmand.osm.LatLon;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ContextMenuLayer extends OsmandMapLayer {
	
	

	public interface IContextMenuProvider {
		
	
		public void collectObjectsFromPoint(PointF point, List<Object> o);
		
		public LatLon getObjectLocation(Object o);
		
		public String getObjectDescription(Object o);
		
		public String getObjectName(Object o);
		
		public DialogInterface.OnClickListener getActionListener(List<String> actionsList, Object o);
	}
	

	private LatLon latLon;
	private String description;
	private IContextMenuProvider selectedContextProvider;
	private List<Object> selectedObjects = new ArrayList<Object>();
	
	private TextView textView;
	private DisplayMetrics dm;
	private OsmandMapTileView view;
	private int BASE_TEXT_SIZE = 170;
	private int SHADOW_OF_LEG = 5;
	
	private final MapActivity activity;
	private Drawable boxLeg;
	private float scaleCoefficient = 1;
	private Rect textPadding;
	public PointF point;
	private ImageView closeButton;
	private int CLOSE_BTN = 3;
	
	public ContextMenuLayer(MapActivity activity){
		this.activity = activity;
	}
	
	@Override
	public void destroyLayer() {
	}

	@Override
	public void initLayer(OsmandMapTileView view) {
		this.view = view;
		dm = new DisplayMetrics();
		WindowManager wmgr = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
		wmgr.getDefaultDisplay().getMetrics(dm);
		scaleCoefficient  = dm.density;
		if (Math.min(dm.widthPixels / (dm.density * 160), dm.heightPixels / (dm.density * 160)) > 2.5f) {
			// large screen
			scaleCoefficient *= 1.5f;
		}
		BASE_TEXT_SIZE = (int) (BASE_TEXT_SIZE * scaleCoefficient);
		SHADOW_OF_LEG = (int) (SHADOW_OF_LEG * scaleCoefficient);
		CLOSE_BTN = (int) (CLOSE_BTN * scaleCoefficient);
		
		boxLeg = view.getResources().getDrawable(R.drawable.box_leg);
		boxLeg.setBounds(0, 0, boxLeg.getMinimumWidth(), boxLeg.getMinimumHeight());
		
		textView = new TextView(view.getContext());
		LayoutParams lp = new LayoutParams(BASE_TEXT_SIZE, LayoutParams.WRAP_CONTENT);
		textView.setLayoutParams(lp);
		textView.setTextSize(15);
		textView.setTextColor(Color.argb(255, 0, 0, 0));
		textView.setMinLines(1);
//		textView.setMaxLines(15);
		textView.setGravity(Gravity.CENTER_HORIZONTAL);
		
		textView.setClickable(true);
		
		textView.setBackgroundDrawable(view.getResources().getDrawable(R.drawable.box_free));
		textPadding = new Rect();
		textView.getBackground().getPadding(textPadding);
		
		closeButton = new ImageView(view.getContext());
		lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		closeButton.setLayoutParams(lp);
		closeButton.setImageDrawable(view.getResources().getDrawable(R.drawable.headline_close_button));
		closeButton.setClickable(true);
		if(latLon != null) {
			setLocation(latLon, description);
		}
	}

	@Override
	public void onDraw(Canvas canvas, RectF latLonBounds, RectF tilesRect, DrawSettings nightMode) {
		if(latLon != null){
			int x = view.getRotatedMapXForPoint(latLon.getLatitude(), latLon.getLongitude());
			int y = view.getRotatedMapYForPoint(latLon.getLatitude(), latLon.getLongitude());
			
			int tx = x - boxLeg.getMinimumWidth() / 2;
			int ty = y - boxLeg.getMinimumHeight() + SHADOW_OF_LEG;
			canvas.translate(tx, ty);
			boxLeg.draw(canvas);
			canvas.translate(-tx, -ty);
			
			if (textView.getText().length() > 0) {
				canvas.translate(x - textView.getWidth() / 2, ty - textView.getBottom() + textPadding.bottom - textPadding.top);
				int c = textView.getLineCount();
				
				textView.draw(canvas);
				canvas.translate(textView.getWidth() - closeButton.getWidth() - CLOSE_BTN, CLOSE_BTN);
				closeButton.draw(canvas);
				if (c == 0) {
					// special case relayout after on draw method
					layoutText();
					view.refreshMap();
				}
			}
		}
	}
	
	private void layoutText() {
		Rect padding = new Rect();
		if (textView.getLineCount() > 0) {
			textView.getBackground().getPadding(padding);
		}
		int w = BASE_TEXT_SIZE;
		int h = (int) ((textView.getPaint().getTextSize() + 4) * textView.getLineCount());
		
		textView.layout(0, -padding.bottom, w, h + padding.top);
		int minw = closeButton.getDrawable().getMinimumWidth();
		int minh = closeButton.getDrawable().getMinimumHeight();
		closeButton.layout(0, 0, minw, minh);
	}
	
	public void setLocation(LatLon loc, String description){
		latLon = loc;
		if(latLon != null){
			if(description == null || description.length() == 0){
				description = view.getContext().getString(R.string.point_on_map, 
						latLon.getLatitude(), latLon.getLongitude());
			}
			textView.setText(description);
		} else {
			textView.setText(""); //$NON-NLS-1$
		}
		layoutText();
	}

	@Override
	public boolean onLongPressEvent(PointF point) {
		if(pressedInTextView(point.x, point.y) > 0){
			setLocation(null, ""); //$NON-NLS-1$
			view.refreshMap();
			return true;
		}
		
		selectedContextProvider = null;
		selectedObjects.clear();
		for(OsmandMapLayer l : view.getLayers()){
			if(l instanceof ContextMenuLayer.IContextMenuProvider){
				((ContextMenuLayer.IContextMenuProvider) l).collectObjectsFromPoint(point, selectedObjects);
				if(!selectedObjects.isEmpty()){
					selectedContextProvider = (IContextMenuProvider) l;
					break;
				}
			}
		}
		
		LatLon latLon = view.getLatLonFromScreenPoint(point.x, point.y);
		StringBuilder description = new StringBuilder(); 
		
		if (!selectedObjects.isEmpty()) {
			if (selectedObjects.size() > 1) {
				description.append("1. ");
			}
			description.append(selectedContextProvider.getObjectDescription(selectedObjects.get(0)));
			for (int i = 1; i < selectedObjects.size(); i++) {
				description.append("\n" + (i + 1) + ". ").append(selectedContextProvider.getObjectDescription(selectedObjects.get(i)));
			}
			LatLon l = selectedContextProvider.getObjectLocation(selectedObjects.get(0));
			if (l != null) {
				latLon = l;
			}
		}
		setLocation(latLon, description.toString());
		view.refreshMap();
		return true;
	}
	
	@Override
	public boolean drawInScreenPixels() {
		return true;
	}
	
	public int pressedInTextView(float px, float py) {
		if (latLon != null) {
			Rect bs = textView.getBackground().getBounds();
			Rect closes = closeButton.getDrawable().getBounds();
			int x = (int) (px - view.getRotatedMapXForPoint(latLon.getLatitude(), latLon.getLongitude()));
			int y = (int) (py - view.getRotatedMapYForPoint(latLon.getLatitude(), latLon.getLongitude()));
			x += bs.width() / 2;
			y += bs.height() + boxLeg.getMinimumHeight() - SHADOW_OF_LEG;
			int dclosex = x - bs.width() + CLOSE_BTN + closes.width();
			int dclosey = y + CLOSE_BTN;
			if(closes.intersects(dclosex - CLOSE_BTN, dclosey - CLOSE_BTN, dclosex + CLOSE_BTN, dclosey + CLOSE_BTN)) {
				return 2;
			}
			else if (bs.contains(x, y)) {
				return 1;
			}
		}
		return 0;
	}
	
	public String getSelectedObjectName(){
		if(!selectedObjects.isEmpty() && selectedContextProvider != null){
			return selectedContextProvider.getObjectName(selectedObjects);
		}
		return null;
	}

	@Override
	public boolean onSingleTap(PointF point) {
		int val = pressedInTextView(point.x, point.y);
		if (val == 2) {
			setLocation(null, ""); //$NON-NLS-1$
			view.refreshMap();
			return true;
		}
		else if (val == 1) {
			if (!selectedObjects.isEmpty()) {
				showContextMenuForSelectedObjects();
			} else {
				activity.contextMenuPoint(latLon.getLatitude(), latLon.getLongitude());
			}
			return true;
		}
		return false;
	}

	private void showContextMenuForSelectedObjects() {
		if(selectedObjects.size() > 1){
			Builder builder = new AlertDialog.Builder(view.getContext());
			String[] d = new String[selectedObjects.size()];
			int i =0;
			for(Object o  : selectedObjects) {
				d[i++] = selectedContextProvider.getObjectDescription(o);
			}
			builder.setItems(d, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// single selection at 0
					selectedObjects.set(0, selectedObjects.get(which));
					ArrayList<String> l = new ArrayList<String>();
					OnClickListener listener = selectedContextProvider.getActionListener(l, selectedObjects.get(0));
					activity.contextMenuPoint(latLon.getLatitude(), latLon.getLongitude(), l, listener);
				}
			});
			builder.show();
		} else {
			ArrayList<String> l = new ArrayList<String>();
			OnClickListener listener = selectedContextProvider.getActionListener(l, selectedObjects.get(0));
			activity.contextMenuPoint(latLon.getLatitude(), latLon.getLongitude(), l, listener);
		}
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (latLon != null) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				int vl = pressedInTextView(event.getX(), event.getY());
				if(vl == 1){
					textView.setPressed(true);
					view.refreshMap();
				} else if (vl == 2) {
					closeButton.setPressed(true);
					view.refreshMap();
				}
			}
		}
		if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			if(textView.isPressed()) {
				textView.setPressed(false);
				view.refreshMap();
			}
			if(closeButton.isPressed()) {
				closeButton.setPressed(false);
				view.refreshMap();
			}
		}
		return false;
	}

	public void setSelectedObject(Object toShow) {
		selectedObjects.clear();
		if(toShow == null){
			selectedContextProvider = null;
		} else {
			for(OsmandMapLayer l : view.getLayers()){
				if(l instanceof ContextMenuLayer.IContextMenuProvider){
					String des = ((ContextMenuLayer.IContextMenuProvider) l).getObjectDescription(toShow);
					if(des != null) {
						selectedContextProvider = (IContextMenuProvider) l;
						selectedObjects.add(toShow);
					}
				}
			}
		}
	}

}
