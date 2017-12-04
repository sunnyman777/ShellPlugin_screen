package com.jiubang.shell.animation;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Transformation3D;
import com.go.gl.view.GLView;

/**
 * 
 * @author zouguiquan
 *
 */
public class AlphaAnimation extends Animation {
	private int mFromAlpha;
	private int mToAlpha;
	private GLView mView;

	public AlphaAnimation(int fromAlpha, int toAlpha, GLView view) {
		mFromAlpha = fromAlpha;
		mToAlpha = toAlpha;
		mView = view;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation3D t) {
		int currentAlpha = (int) (mFromAlpha + (mToAlpha - mFromAlpha) * interpolatedTime);
		if (null != mView) {
			mView.setAlpha(currentAlpha);
		}
	}
}
