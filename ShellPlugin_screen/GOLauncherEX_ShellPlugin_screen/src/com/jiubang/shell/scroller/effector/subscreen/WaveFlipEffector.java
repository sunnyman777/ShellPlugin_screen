package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;

/**
 * 
 * 类描述:渐隐特效
 * 功能详细描述:
 * 
 * @author  jiangxuwen
 * @date  [2013-4-16]
 */
class WaveFlipEffector extends MSubScreenEffector {

	final static int RADIUS = 1;
	final static float SCALEMIN = 0.7f;
    final static float MAX_ANGLE = 150;
    final static int MIN_ALPHA = 30;
	float mRatio;
	float mScaleMin = SCALEMIN;
	float mScaleMax = 1.0f;
    
	public WaveFlipEffector() {
		mCombineBackground = false;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mNeedQuality = false;
		float t = (float) Math.sin(offsetF * mRatio);
		float doubleT = t * t;
		float s = mScaleMax - (mScaleMax - mScaleMin) * doubleT;
		mAlpha = (int) (ALPHA - (ALPHA - MIN_ALPHA) * doubleT);
		float leftTop;
		float cell = 1.0f;
		if (first) {
			leftTop = offsetF + mScreenSize * (1 - s);
		} else {
			leftTop = offsetF;
			cell *= -1;
		}

		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + leftTop, (1 - s) * HALF * mHeight);
		} else {
			canvas.translate((1 - s) * HALF * mWidth, mScroll + leftTop);
		}
		float tx = cell > 0 ? s * mWidth : 0;
		float ty = HALF * mHeight;
		float angleX = (s - mScaleMax) * MAX_ANGLE * cell;
		canvas.translate(tx, ty);
		canvas.rotateAxisAngle(angleX, 0, 1, 0);
		canvas.translate(-tx, -ty);
		canvas.scale(s, s);
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = (float) (Math.PI / 2 / mScreenSize);
	}

}
