package com.jiubang.shell.animation;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Transformation3D;
/**
 * 
 * <br>类描述:用于挤压的时候不断记录当前view的位置的动画，实现在当前位置直接去往下一个位置
 * <br>功能详细描述:
 * 
 * @author  chenjiayu
 * @date  [2012-9-27]
 */
public class TranslateValueAnimation extends Animation {

	private float mInterpolatedTime;
	private float mFirstXDelta; //view的原始x坐标
	private float mFirstYDelta; //view的原始y坐标
	private float mToXDelta; //view的目标x坐标
	private float mToYDelta; // view的目标y坐标
	private float mCurrentXValue = 0.0f; //相对view的原始x坐标的值
	private float mCurrentYValue = 0.0f; //相对view的原始y坐标的值
	private long mDuration = 0; //动画的原始时间

	public TranslateValueAnimation(float fromXDelta, float toXDelta, float fromYDelta,
			float toYDelta, long duration) {
		mFirstXDelta = fromXDelta;
		mToXDelta = toXDelta;
		mFirstYDelta = fromYDelta;
		mToYDelta = toYDelta;
		mDuration = duration;
	}

	private void resetValue(float toXDelta, float toYDelta) {
		mToXDelta = toXDelta;
		mToYDelta = toYDelta;
	}

	public void reStartValueAnimation(float toXDelta, float toYDelta) {
		
		long duration = 0;
		float tempInterpolatedTime = 0f;
		if (mInterpolatedTime >= 1.0f) {
			tempInterpolatedTime = 1.0f;
			duration = mDuration;
		} else {
			tempInterpolatedTime = mInterpolatedTime;
			duration = (long) (mInterpolatedTime * mDuration);
		}
		mCurrentXValue = mCurrentXValue + (mToXDelta - (mFirstXDelta + mCurrentXValue))
				* tempInterpolatedTime;
		mCurrentYValue = mCurrentYValue + (mToYDelta - (mFirstYDelta + mCurrentYValue))
				* tempInterpolatedTime;
		setDuration(duration);
		resetValue(toXDelta, toYDelta);
	}

	public void continueValueAnimation(float toXDelta, float toYDelta) {
		long duration = 0;
		float tempInterpolatedTime = 0f;
		if (mInterpolatedTime > 1.0f) {
			tempInterpolatedTime = 1.0f;
			duration = mDuration;
		} else {
			duration = (long) ((1 - mInterpolatedTime) * mDuration);
			tempInterpolatedTime = mInterpolatedTime;
		}
		mCurrentXValue = mCurrentXValue + (mToXDelta - (mFirstXDelta + mCurrentXValue))
				* tempInterpolatedTime;
		mCurrentYValue = mCurrentYValue + (mToYDelta - (mFirstYDelta + mCurrentYValue))
				* tempInterpolatedTime;
		mDuration = duration;
		setDuration(duration);
		resetValue(toXDelta, toYDelta);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation3D t) {
		mInterpolatedTime = interpolatedTime;
		float dx = mToXDelta - (mFirstXDelta + mCurrentXValue);
		float dy = mToYDelta - (mFirstYDelta + mCurrentYValue);
		if (Math.abs(mFirstXDelta + mCurrentXValue - mToXDelta) > .0000001) {
			dx = mCurrentXValue + (mToXDelta - (mFirstXDelta + mCurrentXValue))
					* interpolatedTime;
		}
		// (mFirstYDelta + mCurrentYValue) != mToYDelta 也可以，但是恒哥说浮点数更好
		if (Math.abs(mFirstYDelta + mCurrentYValue - mToYDelta) > .0000001) {
			dy = mCurrentYValue + (mToYDelta - (mFirstYDelta + mCurrentYValue))
					* interpolatedTime;
		}
		t.setTranslate(dx, dy);
	}

}
