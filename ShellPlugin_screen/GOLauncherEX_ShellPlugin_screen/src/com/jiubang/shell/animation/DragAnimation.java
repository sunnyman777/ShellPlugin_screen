package com.jiubang.shell.animation;

import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.jiubang.shell.drag.DragView;

/**
 * 
 * <br>类描述:此动画是屏幕层长按图标效果的创建类
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 */
public class DragAnimation extends AnimationSet {

	public static final int DURATION_100 = 100;
	public static final int DURATION_200 = 200;
	public static final int DURATION_250 = 250;
	public static final int DURATION_288 = 288;

	private static final float FROM_DEGREE = 45f;
	private static final float TO_DEGREE = -40f;


	public DragAnimation(boolean shareInterpolator, boolean needScale, float scale,
			boolean needShake, DragView dragView) {
		super(shareInterpolator);
		if (needScale && scale != 1.0f) {
			// 放大动画
			Animation scaleAnimation = new ScaleAnimation(1.0f, scale, 1.0f,
					scale, Animation.RELATIVE_TO_SELF,
					AnimationConstant.SCALE_CENTER, Animation.RELATIVE_TO_SELF,
					AnimationConstant.SCALE_CENTER);
			this.addAnimation(scaleAnimation);

		}

		if (needShake) {
			//转动动画
			ShakeAnimation shakeAnim = new ShakeAnimation(FROM_DEGREE, TO_DEGREE);
			this.addAnimation(shakeAnim);
		}
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static class DragAnimationInfo {
		private int mDuration;
		private boolean mNeedScale;
		private boolean mNeedShake;
		private AnimationListener mListener;
		private float mScale = 1.0f;
		public DragAnimationInfo(boolean needScale, float scale, boolean needShake, int duration,
				AnimationListener listener) {
			mNeedScale = needScale;
			mNeedShake = needShake;
			mDuration = duration;
			mListener = listener;
			mScale = scale;
		}

		public int getDuration() {
			return mDuration;
		}

		public boolean isNeedScale() {
			return mNeedScale;
		}

		public boolean isNeedShake() {
			return mNeedShake;
		}

		public AnimationListener getAnimationListener() {
			return mListener;
		}

		public float getScale() {
			return mScale;
		}
	}
}
