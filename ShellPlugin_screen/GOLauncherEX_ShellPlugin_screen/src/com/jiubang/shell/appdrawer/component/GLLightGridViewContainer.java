package com.jiubang.shell.appdrawer.component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.KeyEvent;

import com.go.gl.view.GLFrameLayout;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.shell.appdrawer.controler.StatusFactory;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.indicator.VerticalIndicator;

/**
 * 功能表轻量级的grid容器
 * @author yangguanxiang
 *
 */
public class GLLightGridViewContainer extends GLFrameLayout {

	private DesktopIndicator mHorIndicator;
	private VerticalIndicator mVerIndicator;
	protected GLScrollableBaseGrid mCurGridView;

	public GLLightGridViewContainer(Context context) {
		this(context, null);
		init();
	}

	public GLLightGridViewContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mHorIndicator = new DesktopIndicator(mContext);
		mVerIndicator = new VerticalIndicator(mContext);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int indicatorSize = DrawUtils.dip2px(25);
		if (!mCurGridView.isVerScroll()) {
			mCurGridView.setPadding(getPaddingLeft(), getPaddingTop() + indicatorSize, getPaddingRight(), getPaddingBottom());
			mHorIndicator.layout(0, 0, mWidth, indicatorSize);
		} else {
			mCurGridView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
			DisplayMetrics mMetrics = mContext.getResources().getDisplayMetrics();
			int verIndicatorWidth = (int) (AppFuncConstants.SCROLL_SIZE * mMetrics.density);
			mVerIndicator.layout(mWidth - verIndicatorWidth, 0, mWidth, mHeight);
		}
		mCurGridView.layout(0, 0, mWidth, mHeight);
	}

	public void setGridView(GLScrollableBaseGrid grid) {
		mCurGridView = grid;
		showCurGridView();
	}

	public boolean isScrollFinish() {
		return mCurGridView.isScrollFinish();
	}

	public boolean isVerScroll() {
		return mCurGridView.isVerScroll();
	}

	/**
	 * 通知当前GridView的适配器刷新
	 */
	public void notifyGridDataSetChange() {
		mCurGridView.refreshGridView();
	}

	/**
	 * 通知当前GridView的适配器刷新
	 */
	public void notifyGridDataSetChange(int firstIndex) {
		mCurGridView.layoutPartPage(firstIndex, mCurGridView.getChildCount());
	}

	/**
	 * 显示当前Grid
	 * @param tabId
	 */
	private void showCurGridView() {
		removeAllViews();
		addView(mCurGridView);
		if (!mCurGridView.isVerScroll()) {
			addView(mHorIndicator);
			mCurGridView.setIndicator(mHorIndicator);
		} else {
			addView(mVerIndicator);
			mCurGridView.setIndicator(mVerIndicator);
		}
	}

	/**
	 * 处理滚动设置改变
	 */
	public void handleScrollerSettingChange() {

	}

	/**
	 * 处理滚动循环改变
	 */
	public void handleScrollLoopChange() {
		
	}

	public void handleInidcatorThemeChange() {
		mHorIndicator.applyTheme();
	}

	public void setFontSizeColor(int size, int color) {
		SparseArray<GLExtrusionGridView> gridViewMap = StatusFactory.getGridViewMap();
		GLExtrusionGridView gridView;
		for (int i = 0; i < gridViewMap.size(); i++) {
			gridView = gridViewMap.get(gridViewMap.keyAt(i));
			if (gridView instanceof GLAppDrawerBaseGrid) {
				int count = gridView.getChildCount();
				for (int j = 0; j < count; j++) {
					IconView icon = (IconView) gridView.getViewAtPosition(j);
					icon.setFontSize(size);
					icon.setTitleColor(color);
				}
			}
		}
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		if (mCurGridView != null) {
			mCurGridView.cancelLongPress();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mCurGridView != null) {
			return mCurGridView.onKeyDown(keyCode, event);
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (mCurGridView != null) {
			return mCurGridView.onKeyUp(keyCode, event);
		}
		return false;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (mCurGridView != null) {
			return mCurGridView.onKeyLongPress(keyCode, event);
		}
		return false;
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		if (mCurGridView != null) {
			return mCurGridView.onKeyMultiple(keyCode, repeatCount, event);
		}
		return false;
	}
	
	public void showIndicator(boolean show) {
		mHorIndicator.setVisible(show);
		mVerIndicator.setVisible(show);
	}
}
