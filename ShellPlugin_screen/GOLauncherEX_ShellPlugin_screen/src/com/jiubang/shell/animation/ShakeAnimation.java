package com.jiubang.shell.animation;

import android.util.FloatMath;

import com.go.gl.animation.Animation;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.Transformation3D;

/**
 * 
 * <br>
 * 类描述：通过旋转模拟抖动的动画效果 用于屏幕图标长按 <br>
 * 功能详细描述:
 * 
 * @author yuanzhibiao
 * @date [2012-8-17]
 */
public class ShakeAnimation extends Animation {

	private static final float FROM_DEGREE = -5f;
	private static final float TO_DEGREE = 5f;
	private static final float FROM_DEGREE2 = 15f;
	private static final float TO_DEGREE2 = -12f;

	private static final float T1 = 0.5f;

	private float mFromDegree, mToDegree;
	float mShakeCenterX, mShakeCenterY;

	float mTt = 0, mTtt = 0;
	float mT1 = T1, mT2 = 1f;
	
	private boolean mInitCenter = false;

	public ShakeAnimation(float fromDegree, float toDegree) {
		mFromDegree = fromDegree;
		mToDegree = toDegree;
		mInitCenter = true;
	}
	
	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		
		if (mInitCenter) {	
			mShakeCenterX = width / 2;
			mShakeCenterY = height / 2;
		}
	}

	public ShakeAnimation(float fromDegree, float toDegree, float shakeCenterX,
			float shakeCenterY) {
		mFromDegree = fromDegree;
		mToDegree = toDegree;
		mShakeCenterX = shakeCenterX;
		mShakeCenterY = shakeCenterY;
	}

	@Override
	protected void applyTransformation(float interpolatedTime,
			Transformation3D t) {
		float degrees = 0;
		// 第三次 摆动幅度左右10度
		if (interpolatedTime == 1) {
			//			degrees = getBlanceInterpolation(FROM_DEGREE, TO_DEGREE,
			//					getShakeInterpolation(interpolatedTime));
		} else {

			if (mTt == 1) {
				// 第二次 摆动幅度左右20度
				if (mTtt != 1) {
					mTtt = InterpolatorFactory.remapTime(mT1, mT2, interpolatedTime);
					degrees = getBlanceInterpolation(FROM_DEGREE2, TO_DEGREE2,
							getShakeInterpolation(mTtt));
				}
			} else {
				// 第一次 摆动
				mTt = InterpolatorFactory.remapTime(0, mT1, interpolatedTime);
				degrees = getBlanceInterpolation(mFromDegree, mToDegree, getShakeInterpolation(mTt));
			}
		}
//		if (interpolatedTime < 0.125) {
//			degrees = getBlanceInterpolation(0, -35, interpolatedTime);
//		} else if (interpolatedTime > 0.125 && interpolatedTime <= 0.375) {
//			degrees = getBlanceInterpolation(-35, 30, getShakeInterpolation(interpolatedTime));
//		} else if (interpolatedTime > 0.375 && interpolatedTime <= 0.675) {
//			degrees = getBlanceInterpolation(30, -15, getShakeInterpolation(interpolatedTime));
//		} else if (interpolatedTime > 0.875 && interpolatedTime < 1) {
//			degrees = getBlanceInterpolation(-15, 12, getShakeInterpolation(interpolatedTime));
//		} else if (interpolatedTime == 1) {
//			degrees = getBlanceInterpolation(12, 0, getShakeInterpolation(interpolatedTime));
//		}

		t.setRotate(degrees, mShakeCenterX, mShakeCenterY);
	}

	private float getShakeInterpolation(float t) {
		return FloatMath.sin((float) (t * (Math.PI * 2))) * T1 + T1;
	}

	private float getBlanceInterpolation(float a, float b, float t) {
		return (b - a) * t + a;
	}
}
