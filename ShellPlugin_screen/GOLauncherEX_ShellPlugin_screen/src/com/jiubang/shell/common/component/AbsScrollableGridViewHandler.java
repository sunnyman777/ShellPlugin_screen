package com.jiubang.shell.common.component;

import java.util.ArrayList;

import android.content.Context;
import android.view.MotionEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.shell.drag.DragScroller;
import com.jiubang.shell.indicator.IndicatorListener;
import com.jiubang.shell.indicator.IndicatorUpdateListener;

/**
 * 
 * @author yangguanxiang
 *
 */
public abstract class AbsScrollableGridViewHandler
		implements
			ICleanable,
			IndicatorListener,
			IndicatorUpdateListener,
			DragScroller {

	protected GLScrollableBaseGrid mGridView;
	protected Context mContext;
	protected int mScrollZoneSize;
	protected int mHorizontalSpacing;
	protected int mVerticalSpacing;
	protected boolean mNeedClip = true;
	public AbsScrollableGridViewHandler(Context context, GLScrollableBaseGrid view) {
		mContext = context;
		mGridView = view;
		mScrollZoneSize = mContext.getResources().getDimensionPixelSize(R.dimen.scroll_zone);
	}

	@Override
	public void cleanup() {
		mGridView = null;
		mContext = null;
	}

	public abstract void layoutChildren();

	/**
	 * 局部刷新
	 * @param firstRow
	 * @param lastRow
	 */
	public abstract void layoutChildren(int firstIndex, int lastIndex);

	public abstract void draw(GLCanvas canvas);

	public abstract boolean onTouchEvent(MotionEvent event, int action);

	public abstract boolean isScrollFinished();

	public abstract void setPadding(float paddingFactor);

	public abstract GLView pointToView(MotionEvent event);

	public abstract void setScreenSize(int width, int height);

	public abstract void computeScrollOffset();

	public abstract void setIndicator(GLView indicator);

	public abstract void setCycleScreenMode(boolean mode);

	/**
	 * 行数是从第0行开始算起
	 * @return
	 */
	public abstract int getCurFirstVisiableRow();

	public abstract int getCurLastVisiableRow();

	public abstract void scrollToFirst();

	public abstract void resetOrientation();

	public abstract ArrayList<GLView> getChildren(int index);

	public abstract void clearHolder();

	public abstract void setViewInHolder(int index, GLView view);

	public abstract void removeViewInHolder(int index);

	public abstract void addViewInHolder(GLView view);

	/**
	 * 获取子View的位置（横滑：返回第n屏；竖滑返回第n行）
	 * @param view
	 * @return
	 */
	public abstract int getPositionOfChild(GLView view);

	/**
	 * 获取子View的准确下标位置
	 * @param view
	 * @return
	 */
	public abstract int getIndexOfChild(GLView view);

	public abstract void scrollTo(int idx, boolean needAnimation);

	public abstract int getChildCount();

	public abstract boolean isCircular();

	public abstract boolean resetScrollState();

	/**
	 * 内部行间距
	 * @param horizontalSpacing
	 */
	public void setHorizontalSpacing(int horizontalSpacing) {
		if (horizontalSpacing != mHorizontalSpacing) {
			mHorizontalSpacing = horizontalSpacing;
		}
	}

	/**
	 * 内部列间距
	 * @param verticalSpacing
	 */
	public void setVerticalSpacing(int verticalSpacing) {
		if (verticalSpacing != mVerticalSpacing) {
			mVerticalSpacing = verticalSpacing;
		}
	}

	/**
	 * 内部行间距
	 */
	public int getHorizontalSpacing() {
		return mHorizontalSpacing;
	}

	/**
	 * 内部列间距
	 */
	public int getVerticalSpacing() {
		return mVerticalSpacing;
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static interface ScrollZoneListener {
		public void onEnterLeftScrollZone();
		public void onEnterRightScrollZone();
		public void onEnterTopScrollZone();
		public void onEnterBottomScrollZone();
		public void onExitScrollZone();
	}

	public void setAccFactor(float factor) {
	}

	public void setClipCanvas(boolean clip) {
		mNeedClip = clip;
		invalidate();
	}
	
	public abstract void invalidate();
}
