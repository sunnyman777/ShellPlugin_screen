package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;

/**
 * 
 * <br>类描述:整屏做淡入淡出的特效
 * <br>功能详细描述:
 * 
 * @author  chenjiayu
 * @date  [2012-9-5]
 */
public class AlphaEffector extends MSubScreenEffector {

	float mRatio;
	final int mNum255 = 255;

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = 1.0f / mWidth;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		int oldAlpha = canvas.getAlpha();
		float percent = Math.abs(offsetF) * mRatio;
		int alpha = (int) (mNum255 * (1 - percent));
		canvas.translate(offsetF, 0);
		canvas.translate(mScroll, 0);
		canvas.multiplyAlpha(alpha);
		mContainer.drawScreen(canvas, screen);
		canvas.setAlpha(oldAlpha);
		return false;
	}

}
