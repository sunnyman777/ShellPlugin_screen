package com.jiubang.shell.screen;

import com.go.proxy.SettingProxy;

/**
 * 
 * @author zhujian 有0屏
 */
public class ZeroScreenHandler extends AbsZeroHandler {

	public ZeroScreenHandler(GLSuperWorkspace workspace) {
		super(workspace);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setCureentScreen(int currentScreen) {
		int screenCount = mGLWorkspace.getChildCount();
		mGLWorkspace.mScreenCount = screenCount;
		mGLWorkspace.mCurrentScreen = currentScreen;

		if (mGLWorkspace.mCurrentScreen >= screenCount) {
			mGLWorkspace.mCurrentScreen = screenCount - 1;
		}

		if (screenCount != mGlSuperWorkspace.getScreenScroller()
				.getScreenCount() - 1) {
			mGlSuperWorkspace.getScreenScroller().setScreenCount(
					screenCount + 1);
		}
		mGlSuperWorkspace.getScreenScroller().setCurrentScreen(
				currentScreen + 1);

	}

	@Override
	public int getCureentScreen() {
		// TODO Auto-generated method stub

		if (mGlSuperWorkspace.getScreenScroller().getDstScreen() > 0) {
			mGLWorkspace.mCurrentScreen = mGlSuperWorkspace.getScreenScroller()
					.getDstScreen() - 1;
			return mGLWorkspace.mCurrentScreen;
		} else {
			return mGLWorkspace.mCurrentScreen;
		}

	}

	@Override
	public int snapToScreen(int screen) {
		// TODO Auto-generated method stub
		screen++;
		return screen;
	}

	@Override
	public int getIndicatorItemsSize() {
		// TODO Auto-generated method stub
		int screenCount = mGLWorkspace.getChildCount();
		mGLWorkspace.mScreenCount = screenCount;
		int size = 0;
		size = screenCount + 1;

		return size;
	}

	@Override
	public void setCurrentScreenForMoveScreen() {
		// TODO Auto-generated method stub
		int screenCount = mGLWorkspace.getChildCount();
		mGLWorkspace.mScreenCount = screenCount;
		if (screenCount != mGlSuperWorkspace.getScreenScroller()
				.getScreenCount()) {
			mGlSuperWorkspace.getScreenScroller().setScreenCount(screenCount);
		}
	}

	@Override
	public int getDrawingScreenA() {
		// TODO Auto-generated method stub
		return mGlSuperWorkspace.getScreenScroller().getDrawingScreenA() - 1;
	}

	@Override
	public int getDrawingScreenB() {
		// TODO Auto-generated method stub
		return mGlSuperWorkspace.getScreenScroller().getDrawingScreenB() - 1;
	}

	@Override
	public int onScreenChanged(int newSreen) {
		// TODO Auto-generated method stub
		if (newSreen > 0) {
			newSreen--;
		}
		return newSreen;
	}

	@Override
	public int onDropScreenCount(int screenCount) {
		// TODO Auto-generated method stub
		return screenCount - 1;
	}

	@Override
	public int clickIndicatorItem(int index) {
		// TODO Auto-generated method stub
		index--;
		return index;
	}

	@Override
	public void changeZeroScreenLayout() {
		// TODO Auto-generated method stub
		mGlSuperWorkspace.changeZeroScreenLayout();
	}

	@Override
	public boolean canDoubleTap() {
		if (!mGlSuperWorkspace.isShowingZero()) {
			return true;
		} else {
			if (!mGlSuperWorkspace.isInZeroScreen()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int onDragMultiTouchEventbeyongZero() {
		// TODO Auto-generated method stub
		if (mGLWorkspace.mCurrentScreen == 0) {
			if (SettingProxy.getScreenSettingInfo().mScreenLooping) {
				mGLWorkspace.mCurrentScreen = mGLWorkspace.mCurrentScreen - 1;
				return mGLWorkspace.mCurrentScreen;
			} else {
				return -999;
			}
		}
		return mGLWorkspace.mCurrentScreen;
	}

	@Override
	public int onDragMultiTouchEventBelowZero() {
		// TODO Auto-generated method stub

		if (mGLWorkspace.mCurrentScreen == mGLWorkspace.getChildCount() - 1) {
			if (SettingProxy.getScreenSettingInfo().mScreenLooping) {
				mGLWorkspace.mCurrentScreen = mGLWorkspace.mCurrentScreen + 1;
				return mGLWorkspace.mCurrentScreen;
			} else {
				return -999;
			}
		}
		return mGLWorkspace.mCurrentScreen;
	}

	@Override
	public boolean removeZeroScreen() {
		// TODO Auto-generated method stub
		return mGlSuperWorkspace.removeZeroScreen();
	}

	@Override
	public void addZeroScreen() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isInZeroScreen() {
		// TODO Auto-generated method stub
		return mGlSuperWorkspace.isInZeroScreen();
	}

}
