package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.shell.scroller.ShellScreenScroller;
/**
 * 交替特效
 * @author xiangliang
 *
 */

public class CurveEffector extends MSubScreenEffector {

	final static int RADIUS = 1;
	final static float SCALEMIN = 0.8f;
	final static float MAX_ANGLE = 100;
	final static int MIN_ALPHA = 10;
	float mRatio;
	float mScaleMin = SCALEMIN;
	float mScaleMax = 1.0f;
	int mDockHeight = DockUtil.getBgHeight();

	public CurveEffector() {
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
		float t;
		float doubleT;
		float ws;
		float angle = 0;
		if (first) {
			t = (float) Math.abs(Math.sin(offsetF * mRatio));
			doubleT = t * t;
			ws = 1 + SCALEMIN * doubleT;
		} else {
			t = (float) Math.sin(offsetF * mRatio);
			doubleT = t * t;
			ws = 1 + SCALEMIN * doubleT;
		}
		float tribleT = doubleT * t;
		mAlpha = (int) ((1 - tribleT) * ALPHA);
		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll +  doubleT * offsetF, mDockHeight
					* doubleT);
			angle = t * 90;
			canvas.translate(mWidth * t, mHeight);
			if (first) {
				canvas.rotateAxisAngle((float) (angle / 2 * t), 0, 1, 0);
			} else {
				canvas.rotateAxisAngle((float) (-angle / 2 * t), 0, 1, 0);
			}
			canvas.rotateAxisAngle((float) (-angle * t), 1, 0, 0);
			canvas.translate(-mWidth * t, -mHeight);
			canvas.scale(ws, 1);
		}
		return true;
	}
}
