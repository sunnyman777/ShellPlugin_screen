package com.jiubang.shell.animation;

import android.util.Log;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Transformation3D;
import com.jiubang.shell.drag.DragView;

/**
 * @author zouguiquan
 */
public class BreatheAnimation extends Animation {
	
	private float mMaxScale;
	private float mMinScale;
	private float mCurrentScale;
	private float mMaxAlpha = 255f;
	private float mMinAlpha = 165f;
	private float mCurrentAlpha = 255;
	private DragView mDragView;

	public BreatheAnimation(DragView dragView, float maxScale, float minScale) {
		mMaxScale = maxScale;
		mMinScale = minScale;
		mDragView = dragView;
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation3D t) {

		if (interpolatedTime <= 0.5) {
			mCurrentScale = mMinScale + (mMaxScale - mMinScale) * interpolatedTime;
			mCurrentAlpha = mMinAlpha + (mMaxAlpha - mMinAlpha) * interpolatedTime + 30;
		} else {
			mCurrentScale = mMaxScale - (mMaxScale - mMinScale) * interpolatedTime;
			mCurrentAlpha = mMaxAlpha - (mMaxAlpha - mMinAlpha) * interpolatedTime + 30;
		}
//			if (mZoon) {
//				mCurrentScale = InterpolatorFactory.quadraticEaseIn(mMinScale, mMaxScale,
//						interpolatedTime);
//				mCurrentAlpha = InterpolatorFactory.quadraticEaseIn(mMaxAlpha, mMinAlpha,
//						interpolatedTime);
//				if(mCurrentScale >= mMaxScale && interpolatedTime < 1) {
//					return;
//				}
//				if(interpolatedTime == 1) {
//					mZoon = false;
//				}
//			} else {
//				mCurrentScale = InterpolatorFactory.quadraticEaseOut(mMaxScale, mMinScale,
//						interpolatedTime);
//				if(mCurrentAlpha < 255) {
//					mCurrentAlpha = InterpolatorFactory.quadraticEaseOut(mMinAlpha, mMaxAlpha,
//							interpolatedTime);
//				}
//				if (mCurrentScale == mMinScale && interpolatedTime < 1) {
//					return;
//				}
//				if(interpolatedTime == 1) {
//					mZoon = true;
//				}
//			}
		
		Log.d("animation", "mMinScale = " + mMinScale + " mMaxScale= " + mMaxScale);
		Log.d("animation", "mCurrentScale = " + mCurrentScale + " interpolatedTime= " + interpolatedTime);
		
		mDragView.setAlpha((int) mCurrentAlpha);
		mDragView.setScale(mCurrentScale);
		mDragView.invalidate();
	}
}