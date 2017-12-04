package com.jiubang.shell.animation;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Transformation3D;

/**
 * 
 * @author yangguanxiang
 *
 */
public class TweenAnimation extends Animation {

	private TweenAnimationCallback mCallback;

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation3D t) {
		if (mCallback != null) {
			mCallback.callback(interpolatedTime);
		}
	}

	public void setTweenAnimationCallback(TweenAnimationCallback callback) {
		mCallback = callback;
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static interface TweenAnimationCallback {
		public void callback(float interpolatedTime);
	}
}
