package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;


/**
 * 
 * @author yangguanxiang
 *
 */
class RollEffector extends MSubScreenEffector {
	float mAngleRatio;
	float mDistanceRatio;

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		final float angle = offsetF * mAngleRatio;
		final float dist = offsetF * mDistanceRatio;
		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + dist + mCenterX, mCenterY);
			canvas.rotate(angle);
			canvas.translate(-mCenterX, -mCenterY);
		} else {
			canvas.translate(mCenterX, mScroll + dist + mCenterY);
			canvas.rotate(angle);
			canvas.translate(-mCenterX, -mCenterY);
		}
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mAngleRatio = 180.0f / mScreenSize;
		mDistanceRatio = (float) Math.hypot(mWidth, mHeight) / mScreenSize;
	}

}
