package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;

/**
 * 
 * 类描述:波浪特效
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
class WaveEffector extends MSubScreenEffector {

	final static int RADIUS = 1;
	final static float SCALEMIN = 0.2f;

	float mRatio;
	float mScaleMin = SCALEMIN;
	float mScaleMax = 1.0f;

	public WaveEffector() {
		mCombineBackground = false;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mNeedQuality = false;
		float t = (float) Math.cos(offsetF * mRatio);
		float s = (mScaleMax - mScaleMin) * t * t + mScaleMin;
		float leftTop;
		if (first) {
			leftTop = offsetF + mScreenSize * (1 - s);
		} else {
			leftTop = offsetF;
		}

		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + leftTop, (1 - s) * HALF * mHeight);
		} else {
			canvas.translate((1 - s) * HALF * mWidth, mScroll + leftTop);
		}
		canvas.scale(s, s);
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = (float) Math.PI / (RADIUS * 2 + 1) / mScreenSize;
	}

}
