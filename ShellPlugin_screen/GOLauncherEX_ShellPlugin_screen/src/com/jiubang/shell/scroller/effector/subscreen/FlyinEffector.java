package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;

/**
 * 时光隧道
 * @author xiangliang
 * 
 */
public class FlyinEffector extends MSubScreenEffector {

	float mAlphaRatio;
	float mScaleMin = 0.4f;
	float mScaleMax = 1.0f;

	public FlyinEffector() {
		mCombineBackground = false;
		mReverse = true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mAlphaRatio = (float) Math.PI / 2 / mScreenSize;

	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset,
			boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mNeedQuality = false;
		float t;
		float s;
		float leftTop;
		float doubleT;
		if (first) {
			t = (float) Math.abs(Math.sin(offsetF * mAlphaRatio));
			doubleT = t * t;
			s = mScaleMin * t;
			leftTop = offsetF;
			mAlpha = (int) (ALPHA - ALPHA * doubleT * doubleT);
		} else {
			t = 1 - (float) Math.sin(offsetF * mAlphaRatio);
			s = (mScaleMax - mScaleMin) * t + mScaleMin;
			leftTop = (1 - s) * offsetF + (1 - s) * mScreenSize;
			mAlpha = (int) (ALPHA * t * t);
			;
		}

		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			if (first) {
				canvas.translate(mScroll + leftTop, -s * HALF * mHeight);
				canvas.scale(1 + s, 1 + s);
			} else {
				canvas.translate(mScroll + leftTop, (1 - s) * HALF * mHeight);
				canvas.scale(s, s);
			}
		}
		return true;
	}

}
