package com.jiubang.shell.animation;

import com.go.gl.animation.Animation;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.Transformation3D;

/**
 * 匀速往返不同步旋转动画
 * @author wuziyi
 *
 */
public class AsyncRotateAnimation extends Animation {
    private float mMaxDegrees;
    private float mMinDegrees;

    private int mPivotXType = ABSOLUTE;
    private int mPivotYType = ABSOLUTE;
    private float mPivotXValue = 0.0f;
    private float mPivotYValue = 0.0f;

    private float mPivotX;
    private float mPivotY;
    
    private boolean mIsAsyncMode = true;

	/**
	 * 振幅起始位置偏移值（随机）
	 */
	private float mOffsetDegrees;
	/**
	 * 首次摆动方向（向上／向下）
	 */
	private boolean mIsShakeUp;
	
	private final static float ACTION_FINISH = 1f;
	private final static float HALF_TIME = 0.5f;
	
	private float mFirstActionTime = 0;
	private float mSecoundActionTime = 0;
	private float mThirdActionTime = 0;
	
	private float mFirstActionPersent = 0;
	private float mSecoundActionPersent = 0;
	private float mThirdActionPersent = 0;

    public AsyncRotateAnimation(float maxDegrees, float minDegrees, float pivotX, float pivotY) {
    	mMaxDegrees = maxDegrees;
    	mMinDegrees = minDegrees;
        mPivotXType = ABSOLUTE;
        mPivotYType = ABSOLUTE;
        mPivotXValue = pivotX;
        mPivotYValue = pivotY;
        initPramas();
    }

    public AsyncRotateAnimation(int pivotXType, float pivotXValue,
            int pivotYType, float pivotYValue) {
        mPivotXValue = pivotXValue;
        mPivotXType = pivotXType;
        mPivotYValue = pivotYValue;
        mPivotYType = pivotYType;
        initPramas();
    }
    
    /**
     * 
     * @param pivotXValue 定点X
     * @param pivotYValue 定点Y
     * @param maxDegrees 最大旋转顺时针角度
     * @param minDegrees 最大旋转逆时针角度
     * @param isOutOfSyncMode 是否不同步抖动
     */
    public AsyncRotateAnimation(float pivotXValue, float pivotYValue, float maxDegrees, float minDegrees, boolean isOutOfSyncMode) {
        mPivotXValue = pivotXValue;
        mPivotYValue = pivotYValue;
        mMaxDegrees = maxDegrees;
    	mMinDegrees = minDegrees;
    	
        mIsAsyncMode = isOutOfSyncMode;
        initPramas();
    }

    private void initPramas() {
    	resetActionPersents();
    	if (mIsAsyncMode) {
			mOffsetDegrees = random(mMinDegrees, mMaxDegrees);
//			mOffsetDegrees = 0;
			float persent = InterpolatorFactory.remapTime(mMinDegrees, mMaxDegrees, mOffsetDegrees);
			mIsShakeUp = random(-1, 1) > 0 ? true : false;
//			mIsShakeUp = true;
			mSecoundActionTime = HALF_TIME;
			if (mIsShakeUp) {
				mFirstActionTime = HALF_TIME - HALF_TIME * persent;
			} else {
				mFirstActionTime = HALF_TIME * persent;
			}
			mThirdActionTime = HALF_TIME - mFirstActionTime;
		}
    }
    
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation3D t) {
    	float degrees = 0;
    	if (mFirstActionPersent != ACTION_FINISH) {
    		mFirstActionPersent = InterpolatorFactory.remapTime(0, mFirstActionTime, interpolatedTime);
    		if (mIsShakeUp) {
    			degrees = mOffsetDegrees + (mMaxDegrees - mOffsetDegrees) * mFirstActionPersent;
			} else {
				degrees = mOffsetDegrees + (mMinDegrees - mOffsetDegrees) * mFirstActionPersent;
			}
		} else if (mSecoundActionPersent != ACTION_FINISH) {
			mSecoundActionPersent = InterpolatorFactory.remapTime(mFirstActionTime, mFirstActionTime + mSecoundActionTime, interpolatedTime);
			if (mIsShakeUp) {
				degrees = mMaxDegrees + (mMinDegrees - mMaxDegrees) * mSecoundActionPersent;
			} else {
				degrees = mMinDegrees + (mMaxDegrees - mMinDegrees) * mSecoundActionPersent;
			}
		} else if (mThirdActionPersent != ACTION_FINISH) {
			mThirdActionPersent = InterpolatorFactory.remapTime(mFirstActionTime + mSecoundActionTime, 1, interpolatedTime);
			if (mIsShakeUp) {
				degrees = mMinDegrees + (mOffsetDegrees - mMinDegrees) * mThirdActionPersent;
			} else {
				degrees = mMaxDegrees + (mOffsetDegrees - mMaxDegrees) * mThirdActionPersent;
			}
		}
    	
		if (mPivotX == 0.0f && mPivotY == 0.0f) {
            t.setRotate(degrees);
        } else {
            t.setRotate(degrees, mPivotX, mPivotY);
        }
		
		if (interpolatedTime == 1) {
    		resetActionPersents();
		}
    }
    
    private float calculateDegrees(float toDegrees, float fromDegrees, float actionPersent) {
    	return fromDegrees + (toDegrees - fromDegrees) * actionPersent;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
        mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
    }

    private void resetActionPersents() {
    	mFirstActionPersent = 0;
    	mSecoundActionPersent = 0;
    	mThirdActionPersent = 0;
    }
    
	/**
	 * 生成区间 [a, b) 上均匀分布的随机数
	 * @param a
	 * @param b
	 * @return
	 */
	private static float random(float a, float b) {
		return a + (b - a) * (float) Math.random();
	}
	
	public boolean isAsyncMode() {
		return mIsAsyncMode;
	}

	public void setIsOutOfSyncMode(boolean isOutOfSyncMode) {
		mIsAsyncMode = isOutOfSyncMode;
	}
}
