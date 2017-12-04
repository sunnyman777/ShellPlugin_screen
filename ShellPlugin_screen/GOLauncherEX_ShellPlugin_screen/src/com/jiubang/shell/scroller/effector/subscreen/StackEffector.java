package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;


/**
 * 卡片堆特效
 * @author yangguanxiang
 *
 */
class StackEffector extends MSubScreenEffector {

	final static int RADIUS = 1;

	// float mScaleRatio;
	float mAlphaRatio;
	float mScaleMin = 0.65f;
	float mScaleMax = 1.0f;

	public StackEffector() {
		mCombineBackground = false;
		mReverse = true;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mNeedQuality = false;
		float t;
		float s;
		float leftTop;

		if (first) {
			s = 0;
			leftTop = offsetF;
			mAlpha = 255;
		} else {
			leftTop = offsetF;
//			t = 1 - (float) Math.sin(offset * mAlphaRatio);
//			s = (mScaleMax - mScaleMin) * t + mScaleMin;
//			mAlpha = (int) (255 * t);
			
			t = 1 - (float) offsetF / mScreenSize;
			s = (mScaleMax - mScaleMin) * t + mScaleMin;
			// 前3/13的时间里不进行透明度的变化
			mAlpha = (int) (255 * t * 1.3f);
			mAlpha = mAlpha > 255 ? 255 : mAlpha;
		}

		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			if (first) {
				canvas.translate(mScroll + leftTop, 0);
			} else {
				canvas.translate(mScroll + (1 - s) * 0.5f * mWidth, (1 - s) * 0.5f * mHeight);
				canvas.scale(s, s);
			}
		} else {
			if (first) {
				canvas.translate(0, mScroll + leftTop);
			} else {
				canvas.translate((1 - s) * 0.5f * mWidth, mScroll + (1 - s) * 0.5f * mHeight);
				canvas.scale(s, s);
			}
		}
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		// mScaleRatio = (float)Math.PI / (Radius * 2 + 1) / mScreenSize;
		mAlphaRatio = (float) Math.PI / (RADIUS * 2) / mScreenSize;
	}

}
