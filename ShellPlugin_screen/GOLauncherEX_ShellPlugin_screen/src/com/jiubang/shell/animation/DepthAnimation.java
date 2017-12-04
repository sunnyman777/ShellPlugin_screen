package com.jiubang.shell.animation;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Transformation3D;


/**
 * 将3D空间的近远变化转换成2D投影平面上的缩放变化的动画类
 * 
 * @author dengweiming
 * 
 */
public class DepthAnimation extends Animation {
	private final static float CAMERA_Z = 576; // 摄像机位置的系统默认值
	float mFromZ;
	float mToZ;
//	float mPivotX;
//	float mPivotY;
	float mFromAlpha;
	float mToAlpha;

	public DepthAnimation(float fromZ, float toZ, float fromAlpha, float toAlpha) {
		mFromZ = fromZ;
		mToZ = toZ;
		mFromAlpha = fromAlpha;
		mToAlpha = toAlpha;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation3D t) {
		float z = mFromZ + (mToZ - mFromZ) * interpolatedTime;
		t.setTranslate(0, 0, z);
		float alpha = mFromAlpha + (mToAlpha - mFromAlpha) * interpolatedTime;
		t.setAlpha(alpha);
	}

//	@Override
//	public void initialize(int width, int height, int parentWidth, int parentHeight) {
//		super.initialize(width, height, parentWidth, parentHeight);
//
//		mPivotX = resolveSize(RELATIVE_TO_SELF, 0.5f, width, parentWidth);
//		mPivotY = resolveSize(RELATIVE_TO_SELF, 0.5f, height, parentHeight);
//	}
}