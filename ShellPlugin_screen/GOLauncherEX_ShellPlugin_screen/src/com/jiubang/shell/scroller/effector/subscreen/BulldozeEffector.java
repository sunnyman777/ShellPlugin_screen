package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;

/**
 * 
 * @author yangguanxiang
 *
 */
class BulldozeEffector extends MSubScreenEffector {
	public BulldozeEffector() {
		super();
		mCombineBackground = false;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mNeedQuality = false;
		float min, max;

		if (first) {
			min = 0;
			max = offsetF + mScreenSize;
		} else {
			min = offsetF;
			max = mScreenSize;
		}

		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + min, 0);
			canvas.scale((float) (max - min) / mWidth, 1);
		} else {
			canvas.translate(0, mScroll + min);
			canvas.scale(1, (float) (max - min) / mHeight);
		}
		return true;
	}
}
