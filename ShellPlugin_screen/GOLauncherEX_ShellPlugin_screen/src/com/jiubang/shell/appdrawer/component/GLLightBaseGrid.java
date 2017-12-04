package com.jiubang.shell.appdrawer.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.common.component.GLNoDataView;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.component.VerScrollableGridViewHandler;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;

/**
 * 轻量级GridView
 * @author yangguanxiang
 *
 */
public abstract class GLLightBaseGrid extends GLExtrusionGridView {

	protected boolean mNoData;
	protected GLNoDataView mNoDataView;

	public GLLightBaseGrid(Context context) {
		super(context);
		init();
	}

	public GLLightBaseGrid(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GLLightBaseGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		initNoDataView();
	}

	protected void initNoDataView() {
		mNoDataView = new GLNoDataView(mContext);
		GLScrollableBaseGrid.LayoutParams p = new GLScrollableBaseGrid.LayoutParams(
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT,
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT);
		mNoDataView.setLayoutParams(p);
		addViewInLayout(mNoDataView, 0, p, true);
		mNoDataView.setNoDataText(getNoDataText());
	}

	public abstract String getNoDataText();

	@Override
	protected void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
		if (isChanged) {
			handleRowColumnSetting(true);
		}
		super.onLayout(isChanged, left, top, right, bottom);
		if (mNoData) {
			mNoDataView.measure(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
			mNoDataView.layout(left, top, right, bottom);
		}
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (!mNoData) {
			super.dispatchDraw(canvas);
		} else {
			mNoDataView.draw(canvas);
		}
	}

	@Override
	public synchronized boolean onTouchEvent(MotionEvent ev) {
		if (!mNoData) {
			return super.onTouchEvent(ev);
		} else {
			return false;
		}
	}
	
	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
	}
	
	@Override
	public void onDragMove(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
	}
	
	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo) {
		mIconOperation.resetDragIcon(resetInfo);
		return true;
	}

	@Override
	public void dataChangeOnMoveStart(Object dragInfo, int targetIndex, int sourceIndex) {
		
	}
	
	@Override
	public void dataChangeOnMoveEnd(Object dragInfo, int targetIndex,
			int sourceIndex) {
		
	}

	@Override
	public void dataChangeOnDrop(Object dragInfo, int targetIndex, int sourceIndex) {
		
	}
	
	public ArrayList<GLView> getCurScreenIcons() {
		ArrayList<GLView> iconList;
		if (isVerScroll()) {
			VerScrollableGridViewHandler handler = (VerScrollableGridViewHandler) mScrollableHandler;
			//			handler.setClipCanvas(false);
			int firstRow = handler.getCurFirstVisiableRow();
			int lastRow = handler.getCurLastVisiableRow();
			iconList = new ArrayList<GLView>();
			for (int i = firstRow; i <= lastRow; i++) {
				List<GLView> iconListPerRow = mScrollableHandler.getChildren(i);
				iconList.addAll(iconListPerRow);
			}
		} else {
			int currentScreen = ((HorScrollableGridViewHandler) mScrollableHandler)
					.getCurrentScreen();
			iconList = mScrollableHandler.getChildren(currentScreen);
		}
		return iconList;
	}

}
