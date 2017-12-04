package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.scroller.ScreenScroller;

/**
 * 
 */
public class BounceEffector extends MSubScreenEffector {
	float mRatio;

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mNeedQuality = false;
		float tranY = -offsetF * offsetF * mRatio;
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + offsetF, tranY);
		} else {
			canvas.translate(tranY, mScroll + offsetF);
		}
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			mRatio = (float) mHeight / mWidth / mWidth;
		} else {
			mRatio = (float) mWidth / mHeight / mHeight;
		}
	}
}
