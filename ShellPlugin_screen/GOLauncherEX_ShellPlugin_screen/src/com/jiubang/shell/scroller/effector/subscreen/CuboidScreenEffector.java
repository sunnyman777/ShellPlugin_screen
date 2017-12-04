package com.jiubang.shell.scroller.effector.subscreen;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScreenScroller;

/**
 * 
 * 类描述:长方体特效
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public class CuboidScreenEffector extends CuboidOutsideEffector {

	float mRatio;

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = 1.0f / mWidth;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		mScroller.setMaxOvershootPercent(0);

		final float angle = offsetF * mAngleRatio;
		final float angleAbs = Math.abs(angle);
		float tempAngle = (Math.abs(mScroll) % mWidth) * mAngleRatio;
		float distance = 0;
		if (tempAngle <= RIGHT_ANGLE / 2) {
			distance = (float) (Math.sin(tempAngle * (Math.PI / HALF_ANGLE)) * (mWidth / 2));
		} else if (tempAngle > RIGHT_ANGLE / 2 && tempAngle <= RIGHT_ANGLE) {
			distance = (float) (Math.cos(tempAngle * (Math.PI / HALF_ANGLE)) * (mWidth / 2));
		}
		//mCullFailAngle原来是这个
		if (angleAbs > RIGHT_ANGLE) {
			return false;
		}
		//计算当前透明度的值，0.8是为了调节快慢而设的参数
		float percentAlpha = (float) ((Math.abs(offsetF) / (mWidth * 0.8)) * Math.sin(Math   // CHECKSTYLE IGNORE THIS LINE
				.abs(offsetF * 0.8) * mAngleRatio * (Math.PI / HALF_ANGLE)));                // CHECKSTYLE IGNORE THIS LINE
		if (percentAlpha > 1) {
			percentAlpha = 1;
		}

		//保存canvas的原有alpha值
		int oldAlpha = canvas.getAlpha();

		canvas.save();
		canvas.translate(0, 0, -distance);
		//垂直改变角度
		transformView(canvas, angle);
		canvas.setCullFaceEnabled(false);
		canvas.save();
		if (first) {
			canvas.rotateAxisAngle(-90, 0, 1, 0);
			canvas.translate(-mScroller.getScreenWidth(), 0);
			((CustomSubScreenContainer) mContainer).drawScreenBackground(canvas, screen);
		} else {
			canvas.translate(mScroller.getScreenWidth(), 0);
			canvas.rotateAxisAngle(-90, 0, 1, 0);
			canvas.translate(-mScroller.getScreenWidth(), 0);
			((CustomSubScreenContainer) mContainer).drawScreenBackground(canvas, screen);
		}
		canvas.restore();
		//画背景
		((CustomSubScreenContainer) mContainer).drawScreenBackground(canvas, screen);
		canvas.setCullFaceEnabled(true);
		canvas.restore();
		drawAll(canvas, percentAlpha, oldAlpha, screen, offsetF, angle);
		return false;
	}

	private void drawScreenContent(GLCanvas canvas, float percent, int oldAlpha, int screen,
			float offset) {
		canvas.translate(offset, 0);
		canvas.multiplyAlpha((int) (ALPHA * (1 - percent)));
		mContainer.drawScreen(canvas, screen);
		canvas.setAlpha(oldAlpha);
	}

	private void transformView(GLCanvas canvas, float angleY) {
		if (mVerticalSlide) {
			float angleX = getAngleX(Math.min(mRatio * Math.abs(mScroller.getCurrentScreenOffset())
					* 2, 1));
			transformXandY(canvas, angleY, angleX);
		} else {
			transform(canvas, angleY);
		}
	}

	private void drawAll(GLCanvas canvas, float percent, int oldAlpha, int screen, float offset,
			float angle) {
		//垂直改变角度
		transformView(canvas, angle);
		drawScreenContent(canvas, percent, oldAlpha, screen, offset);
	}
	
	@Override
	protected boolean toReverse() {
		return mScroller.getCurrentScreen() == mScroller.getDrawingScreenA();
	}
	
	@Override
	protected void drawView(GLCanvas canvas, int screen, int offset,
			boolean first) {
		if (screen == ShellScreenScroller.INVALID_SCREEN) {
			if (screen == mScroller.getDrawingScreenB()) {
				screen = mScroller.getDrawingScreenA() + 1;
			} else if (screen == mScroller.getDrawingScreenA()) {
				screen = mScroller.getDrawingScreenB() + 1;
			}
			canvas.save();
			onDrawScreen(canvas, screen, offset, first);
			canvas.restore();
			return;
		}
		int saveCount = canvas.save();
		if (onDrawScreen(canvas, screen, offset, first)) {
			if (mCombineBackground) {
				mScroller.drawBackgroundOnScreen(canvas, screen);
			}
			if (mAlpha == ALPHA) {
				mContainer.drawScreen(canvas, screen);
			} else if (mAlpha > 0) {

				mContainer.drawScreen(canvas, screen, mAlpha);

			}
		}
		canvas.restoreToCount(saveCount);
	}
	
}