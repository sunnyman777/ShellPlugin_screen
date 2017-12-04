package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;
/**
 * 淡入淡出
 * @author xiangliang
 *
 */
public class CrossFadeEffector extends MSubScreenEffector {

	final static int RADIUS = 1;
	final static float SCALEMIN = 0.7f;
	final static float MAX_ANGLE = 100;
	final static int MIN_ALPHA = 10;
	float mRatio;
	float mScaleMin = SCALEMIN;
	float mScaleMax = 1.0f;

	public CrossFadeEffector() {
		mCombineBackground = false;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = (float) (Math.PI / 2 / mScreenSize);
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset,
			boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mNeedQuality = false;
		float t = (float) Math.sin(offsetF * mRatio);
		float doubleT = t * t;
		float s = mScaleMax - (mScaleMax - mScaleMin) * doubleT;
		mAlpha = (int) (ALPHA - (ALPHA - MIN_ALPHA) * doubleT);

		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll, 0);
		} else {
			canvas.translate((1 - s) * HALF * mWidth, 0);
		}
		return true;
	}
}
