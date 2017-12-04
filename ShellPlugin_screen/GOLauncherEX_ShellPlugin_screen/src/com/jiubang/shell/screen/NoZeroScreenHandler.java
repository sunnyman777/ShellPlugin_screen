package com.jiubang.shell.screen;


/**
 * 
 * @author zhujian 没0屏
 */
public class NoZeroScreenHandler extends AbsZeroHandler {

	public NoZeroScreenHandler(GLSuperWorkspace workspace) {
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

		if (screenCount != mGlSuperWorkspace.getScreenScroller().getScreenCount()) {
			mGlSuperWorkspace.getScreenScroller().setScreenCount(screenCount);
		}
		mGlSuperWorkspace.getScreenScroller().setCurrentScreen(currentScreen);
	}

	@Override
	public int getCureentScreen() {
		// TODO Auto-generated method stub
		mGLWorkspace.mCurrentScreen = mGlSuperWorkspace.getScreenScroller().getDstScreen();
		return mGLWorkspace.mCurrentScreen;

	}

	@Override
	public int snapToScreen(int screen) {
		// TODO Auto-generated method stub
		return screen;
	}

	@Override
	public int getIndicatorItemsSize() {
		// TODO Auto-generated method stub
		return mGLWorkspace.getChildCount();
	}

	@Override
	public void setCurrentScreenForMoveScreen() {
		// TODO Auto-generated method stub
		mGLWorkspace.mScreenCount = mGLWorkspace.getChildCount();
		if (mGLWorkspace.mScreenCount != mGlSuperWorkspace.getScreenScroller().getScreenCount() - 1) {
			mGlSuperWorkspace.getScreenScroller().setScreenCount(mGLWorkspace.mScreenCount + 1);
		}
	}

	@Override
	public int getDrawingScreenA() {
		// TODO Auto-generated method stub
		return mGlSuperWorkspace.getScreenScroller().getDrawingScreenA();
	}

	@Override
	public int getDrawingScreenB() {
		// TODO Auto-generated method stub
		return mGlSuperWorkspace.getScreenScroller().getDrawingScreenB();
	}

	@Override
	public int onScreenChanged(int newScreen) {
		// TODO Auto-generated method stub
		return newScreen;
	}

	@Override
	public int onDropScreenCount(int screenCount) {
		// TODO Auto-generated method stub
		return screenCount;
	}

	@Override
	public int clickIndicatorItem(int index) {
		// TODO Auto-generated method stub
		return index;
	}

	@Override
	public void changeZeroScreenLayout() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canDoubleTap() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int onDragMultiTouchEventbeyongZero() {
		// TODO Auto-generated method stub
		return mGLWorkspace.mCurrentScreen;
	}

	@Override
	public int onDragMultiTouchEventBelowZero() {
		// TODO Auto-generated method stub
		return mGLWorkspace.mCurrentScreen;
	}

	@Override
	public boolean removeZeroScreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addZeroScreen() {
		// TODO Auto-generated method stub
		mGlSuperWorkspace.addZeroScreen();
	}

	@Override
	public boolean isInZeroScreen() {
		// TODO Auto-generated method stub
		return false;
	}

}
