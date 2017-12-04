package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;
/**
 * 翻页特效
 * @author xiangliang
 *
 */
public class PageturnEffector extends MSubScreenEffector {

	float mAlphaRatio;
	private final static int MINALPHA = 10;

	public PageturnEffector() {
		mCombineBackground = false;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset,
			boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mNeedQuality = false;
		float t;
		float angle = 0;
		if (first) {
			t = (float) Math.abs(Math.sin(offsetF * mAlphaRatio));
			mAlpha = (int) (ALPHA - (ALPHA - MINALPHA) * t);
		} else {
			t = 1 - (float) Math.sin(offsetF * mAlphaRatio);
			mAlpha = (int) ((ALPHA - MINALPHA) * t + MINALPHA);
		}
		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			if (first) {
				angle = -90 * t;
				canvas.translate(mScroll, mHeight / 2);
				canvas.rotateAxisAngle(angle, 0, 1, 0);
				canvas.translate(0, -mHeight / 2);
			} else {
				canvas.translate(mScroll + mScreenSize, mHeight / 2);
				angle = (float) (90 * t + 270 + 12 * (t - 1)); // 后面的偏移量是因为3d的270度并不是视觉上的位置
				canvas.rotateAxisAngle(angle, 0, 1, 0);
				canvas.translate(-mScreenSize, -mHeight / 2);
			}
		}
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mAlphaRatio = (float) Math.PI / 2 / mScreenSize;

	}

}