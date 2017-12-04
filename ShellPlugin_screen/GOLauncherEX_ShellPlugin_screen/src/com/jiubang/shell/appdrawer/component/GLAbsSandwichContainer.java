package com.jiubang.shell.appdrawer.component;

import android.content.Context;
import android.view.KeyEvent;

import com.go.proxy.GoLauncherActivityProxy;

/**
 * 三层架构组件基类
 * @author yangguanxiang
 *
 */
public abstract class GLAbsSandwichContainer extends GLAbsExtendFuncView {

	protected GLTopBarContainer mTopBarContainer;
	protected GLBottomBarContainer mBottomBarContainer;
	protected GLLightGridViewContainer mGridViewContainer;

	protected int mTopBarHeightV;
	protected int mBottomBarHeightV;
	protected int mTopBarHeightH;
	protected int mBottomBarHeightH;

	public GLAbsSandwichContainer(Context context) {
		this(context, 0, 0, 0, 0);
	}

	public GLAbsSandwichContainer(Context context, int topBarHeightV, int bottomBarHeightV,
			int topBarHeightH, int bottomBarHeightH) {
		super(context);
		setStatusBarPadding();
		mTopBarHeightV = topBarHeightV;
		mBottomBarHeightV = bottomBarHeightV;

		mTopBarHeightH = topBarHeightH;
		mBottomBarHeightH = bottomBarHeightH;

		if (mTopBarHeightV > 0 || mTopBarHeightH > 0) {
			mTopBarContainer = new GLTopBarContainer(context) {
				@Override
				public void setVerticalMode(boolean isVertical) {
					mIsVerticalMode = true;
				}
			};
			addView(mTopBarContainer);
		}
		mGridViewContainer = new GLLightGridViewContainer(context);
		addView(mGridViewContainer);
		if (mBottomBarHeightV > 0 || mBottomBarHeightH > 0) {
			mBottomBarContainer = new GLBottomBarContainer(context) {
				@Override
				public void setVerticalMode(boolean isVertical) {
					mIsVerticalMode = true;
				}
			};
			addView(mBottomBarContainer);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int paddingTop = getPaddingTop();
		int topBarHeight = mTopBarHeightH;
		int bottomBarHeight = mBottomBarHeightH;
		if (GoLauncherActivityProxy.isPortait()) {
			topBarHeight = mTopBarHeightV;
			bottomBarHeight = mBottomBarHeightV;
		}
		if (mTopBarContainer != null) {
			mTopBarContainer.layout(0, paddingTop, mWidth, paddingTop + topBarHeight);
		}
		if (mBottomBarContainer != null) {
			mBottomBarContainer.layout(0, mHeight - bottomBarHeight, mWidth, mHeight);
		}
		mGridViewContainer.layout(0, paddingTop + topBarHeight, mWidth, mHeight - bottomBarHeight);
	}

	public void notifyGridDataSetChange() {
		mGridViewContainer.notifyGridDataSetChange();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mGridViewContainer.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mGridViewContainer.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		return mGridViewContainer.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return mGridViewContainer.onKeyMultiple(keyCode, repeatCount, event);
	}
}
