package com.jiubang.shell.animation;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Transformation3D;

/**
 * 
 * <br>类描述: 3D旋转动画
 * <br>功能详细描述:
 * 
 * @date  [2012-9-5]
 */
public class Rotate3DAnimation extends Animation {
	//	Transformation3D mTempTransformation3d = new Transformation3D();
	private float mFromDegrees;
	private float mToDegrees;

	private float mAxisXValue;
	private float mAxisYValue;
	private float mAxisZValue;

	private float mPivotXValue = 0.0f;
	private float mPivotYValue = 0.0f;
	private float mPivotZValue = 0.0f;

	private float mPivotX;
	private float mPivotY;
	private float mPivotZ;
	
	private int mPivotXType = ABSOLUTE;
	private int mPivotYType = ABSOLUTE;
	private int mPivotZType = ABSOLUTE;

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation3D t) {
		final float angle = mFromDegrees + (mToDegrees - mFromDegrees) * interpolatedTime;
		if (mPivotX == 0.0f && mPivotY == 0.0f && mPivotZ == 0.0f) {
			t.setRotateAxisAngle(angle, mAxisXValue, mAxisYValue, mAxisZValue);
		} else {
			t.setRotateAxisAngle(angle, mAxisXValue, mAxisYValue, mAxisZValue, mPivotX, mPivotY,
					mPivotZ);
		}
	}

	/**
	* Constructor to use when building a RotateAnimation from code
	* 
	* @param fromDegrees Rotation offset to apply at the start of the
	*        animation.
	* 
	* @param toDegrees Rotation offset to apply at the end of the animation.
	* 
	* @param axisX/axisY/axisZ The X/Y/Z coordinate of the rotate axis
	*        
	*/
	public Rotate3DAnimation(float fromDegrees, float toDegrees, float axisX, float axisY,
			float axisZ) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;

		mAxisXValue = axisX;
		mAxisYValue = axisY;
		mAxisZValue = axisZ;

		mPivotXValue = 0.0f;
		mPivotYValue = 0.0f;
		mPivotZValue = 0.0f;
	}

	/**
	 * Constructor to use when building a RotateAnimation from code
	 * 
	 * @param fromDegrees Rotation offset to apply at the start of the
	 *        animation.
	 * 
	 * @param toDegrees Rotation offset to apply at the end of the animation.
	 * 
	 * @param pivotX/pivotY/pivotZ The X/Y/Z coordinate of the point about which the object is
	 *        being rotated, specified as an absolute number where 0 is the left
	 *        edge.
	 * @param axisX/axisY/axisZ The X/Y/Z coordinate of the rotate axis
	 */
	public Rotate3DAnimation(float fromDegrees, float toDegrees, float pivotX, float pivotY,
			float pivotZ, float axisX, float axisY, float axisZ) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;

		mPivotXValue = pivotX;
		mPivotYValue = pivotY;
		mPivotZValue = pivotZ;

		mAxisXValue = axisX;
		mAxisYValue = axisY;
		mAxisZValue = axisZ;
	}

	public Rotate3DAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotX,
			int pivotYType, float pivotY, int pivotZType, float pivotZ, float axisX, float axisY,
			float axisZ) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;

		mPivotXValue = pivotX;
		mPivotYValue = pivotY;
		mPivotZValue = pivotZ;

		mAxisXValue = axisX;
		mAxisYValue = axisY;
		mAxisZValue = axisZ;

		mPivotXType = pivotXType;
		mPivotYType = pivotYType;
		mPivotZType = pivotZType;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
		mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
		mPivotZ = resolveSize(mPivotZType, mPivotZValue, height, parentHeight);
	}
}